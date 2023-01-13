package finance.tegro.observer.service

import finance.tegro.core.contract.SwapParams
import finance.tegro.core.contract.op.*
import finance.tegro.core.entity.Swap
import finance.tegro.core.repository.ExchangePairRepository
import finance.tegro.core.repository.SwapRepository
import finance.tegro.core.toSafeString
import finance.tegro.observer.job.LiquidityJob
import finance.tegro.observer.job.ReserveJob
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import mu.KLogging
import org.quartz.*
import org.springframework.stereotype.Service
import org.ton.block.*
import org.ton.cell.Cell
import org.ton.tlb.exception.UnknownTlbConstructorException
import org.ton.tlb.loadTlb
import java.time.Instant

@Service
class ExchangePairService(
    private val scheduler: Scheduler,
    private val exchangePairRepository: ExchangePairRepository,
    private val swapRepository: SwapRepository,
    private val transactionService: TransactionService,
) : CoroutineScope {
    override val coroutineContext = Dispatchers.IO + CoroutineName("ExchangePairService")

    val job = launch {
        transactionService.data
            // Only interested in internal messages
            .filter { (_, transaction) -> transaction.in_msg.value?.info is IntMsgInfo && transaction.out_msgs.firstOrNull()?.second?.info is IntMsgInfo }
            .filter { (blockId, transaction) -> // Filter out txs not to exchange pairs
                exchangePairRepository.existsByAddress(AddrStd(blockId.workchain, transaction.account_addr))
            }
            .onEach { (blockId, transaction) -> // Trigger reserve update
                val address = AddrStd(blockId.workchain, transaction.account_addr)

                val reserveJobKey =
                    JobKey("ReserveJob_${address.toSafeString()}_${blockId.id}", "ReserveJob")

                if (!scheduler.checkExists(reserveJobKey))
                    scheduler.scheduleJob(
                        JobBuilder.newJob(ReserveJob::class.java)
                            .withIdentity(reserveJobKey)
                            .usingJobData(
                                JobDataMap(
                                    mapOf(
                                        "address" to address,
                                        "blockId" to blockId
                                    )
                                )
                            )
                            .build(),
                        TriggerBuilder.newTrigger()
                            .withIdentity(
                                "ReserveTrigger_${address.toSafeString()}_${blockId.id}",
                                "ReserveTrigger"
                            )
                            .startNow()
                            .build()
                    )
            }
            .flowOn(Dispatchers.IO)
            .collect { (blockId, transaction) ->
                val exchangePair = AddrStd(blockId.workchain, transaction.account_addr)

                // TODO: This is fucking ugly
                val inMsgOp = transaction.in_msg.value?.parseOp()?.let {
                    if (it is SuccessfulSwapOp) { // Routing is happening here, simply unwrap inner op
                        logger.debug { "routed operation: $it" }
                        it.payload.value?.refs?.firstOrNull()?.parse { loadTlb(SwapParams) }?.let { params ->
                            SwapTonOp(
                                queryId = it.queryId,
                                params = params,
                            )
                        }
                    } else {
                        it
                    }
                }
                val inMsgInfo = transaction.in_msg.value?.info as IntMsgInfo
                val outMsgOp = transaction.out_msgs.firstOrNull()?.second?.parseOp()
                val outMsgInfo = transaction.out_msgs.firstOrNull()?.second?.info as IntMsgInfo

                if (inMsgOp is SwapTonOp && outMsgOp is TransferOp && outMsgOp.forwardPayloadOp() is SuccessfulSwapPayloadOp) {
                    // Successful TON -> Jetton swap
                    withContext(Dispatchers.IO) {
                        swapRepository.save(
                            Swap(
                                destination = outMsgInfo.dest,
                                baseAmount = inMsgInfo.value.coins.amount.value - outMsgInfo.value.coins.amount.value,
                                exchangePair = exchangePair,
                                quoteAmount = outMsgOp.amount.value,
                                inverse = false, // Base (TON) -> Quote (Token) swap
                                referrer = parseOpReferrer(inMsgOp.params.customPayload),
                                queryId = outMsgOp.queryId,
                                transaction = transaction,
                                blockId = blockId,
                                timestamp = Instant.now(),
                            )
                        )
                    }
                        .also { logger.debug { "${it.queryId}: ${it.baseAmount} TON -> ${it.quoteAmount} Jetton" } }
                } else if (inMsgOp is TransferNotificationOp && inMsgOp.forwardPayloadOp() is SwapJettonPayloadOp && outMsgOp is SuccessfulSwapOp) {
                    // Successful Jetton -> TON swap
                    withContext(Dispatchers.IO) {
                        swapRepository.save(
                            Swap(
                                destination = outMsgInfo.dest,
                                baseAmount = outMsgInfo.value.coins.amount.value - inMsgInfo.value.coins.amount.value,
                                exchangePair = exchangePair,
                                quoteAmount = inMsgOp.amount.value,
                                inverse = true, // Quote (Token) -> Base (TON) swap
                                referrer = parseOpReferrer((inMsgOp.forwardPayloadOp() as SwapJettonPayloadOp).params.customPayload),
                                queryId = outMsgOp.queryId,
                                transaction = transaction,
                                blockId = blockId,
                                timestamp = Instant.now(),
                            )
                        )
                    }
                        .also { logger.debug { "${it.queryId}: ${it.quoteAmount} Jetton -> ${it.baseAmount} TON" } }
                } else if (inMsgOp is TransferNotificationOp && inMsgOp.forwardPayloadOp() is AddLiquidityPayloadOp && outMsgOp is TransferOp) {
                    // Successful liquidity deposit
                    val liquidityJobKey =
                        JobKey("LiquidityJob_${inMsgInfo.src.toSafeString()}_${blockId.id}", "LiquidityJob")

                    if (!scheduler.checkExists(liquidityJobKey))
                        scheduler.scheduleJob(
                            JobBuilder.newJob(LiquidityJob::class.java)
                                .withIdentity(liquidityJobKey)
                                .usingJobData(
                                    JobDataMap(
                                        mapOf(
                                            "owner" to inMsgOp.sender,
                                            "exchangePair" to exchangePair,
                                            "blockId" to blockId
                                        )
                                    )
                                )
                                .build(),
                            TriggerBuilder.newTrigger()
                                .withIdentity(
                                    "LiquidityTrigger_${inMsgInfo.src.toSafeString()}_${blockId.id}",
                                    "LiquidityTrigger"
                                )
                                .startNow()
                                .build()
                        )
                } else if (inMsgOp is BurnNotificationOp && outMsgOp is InternalTransferOp) {
                    // Successful liquidity withdrawal
                    val liquidityJobKey =
                        JobKey("LiquidityJob_${inMsgInfo.src.toSafeString()}_${blockId.id}", "LiquidityJob")

                    if (!scheduler.checkExists(liquidityJobKey))
                        scheduler.scheduleJob(
                            JobBuilder.newJob(LiquidityJob::class.java)
                                .withIdentity(liquidityJobKey)
                                .usingJobData(
                                    JobDataMap(
                                        mapOf(
                                            "owner" to inMsgOp.sender,
                                            "exchangePair" to exchangePair,
                                            "blockId" to blockId
                                        )
                                    )
                                )
                                .build(),
                            TriggerBuilder.newTrigger()
                                .withIdentity(
                                    "LiquidityTrigger_${inMsgInfo.src.toSafeString()}_${blockId.id}",
                                    "LiquidityTrigger"
                                )
                                .startNow()
                                .build()
                        )
                } else {
                    logger.warn { "$blockId: unhandled $inMsgOp -> $outMsgOp" }
                }
            }
    }

    private fun Message<Cell>.parseOp() =
        try {
            (body.x ?: body.y)
                ?.parse { loadTlb(Op) }
        } catch (e: UnknownTlbConstructorException) {
            logger.warn(e) { "Unknown tlb constructor" }
            null
        }

    private fun TransferNotificationOp.forwardPayloadOp() =
        try {
            (forwardPayload.x ?: forwardPayload.y)
                ?.parse { loadTlb(PayloadOp) }
        } catch (e: UnknownTlbConstructorException) {
            logger.warn(e) { "Unknown tlb constructor" }
            null
        }

    private fun TransferOp.forwardPayloadOp() =
        try {
            (forwardPayload.x ?: forwardPayload.y)
                ?.parse { loadTlb(PayloadOp) }
        } catch (e: UnknownTlbConstructorException) {
            logger.warn(e) { "Unknown tlb constructor" }
            null
        }

    private fun parseOpReferrer(cell: Maybe<Cell>): MsgAddress =
        if ((cell.value?.refs?.size ?: 0) > 0) {
            parseOpReferrer(Maybe.of(cell.value?.refs?.first()))
        } else {
            try {
                cell.value?.parse { loadTlb(MsgAddress) } ?: AddrNone
            } catch (e: Exception) {
                AddrNone
            }
        }

    companion object : KLogging()
}
