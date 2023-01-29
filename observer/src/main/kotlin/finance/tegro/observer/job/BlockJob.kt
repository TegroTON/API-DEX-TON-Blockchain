package finance.tegro.observer.job

import finance.tegro.core.contract.SwapParams
import finance.tegro.core.contract.op.*
import finance.tegro.core.entity.BlockId
import finance.tegro.core.entity.Swap
import finance.tegro.core.repository.BlockIdRepository
import finance.tegro.core.repository.ExchangePairRepository
import finance.tegro.core.repository.SwapRepository
import finance.tegro.core.repository.TokenRepository
import finance.tegro.core.toSafeString
import finance.tegro.observer.properties.BlockIdServiceProperties
import kotlinx.coroutines.*
import mu.KLogging
import org.quartz.*
import org.quartz.Job
import org.springframework.stereotype.Component
import org.ton.api.tonnode.Shard
import org.ton.api.tonnode.TonNodeBlockId
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.bigint.BigInt
import org.ton.block.*
import org.ton.cell.Cell
import org.ton.lite.client.LiteClient
import org.ton.tlb.exception.UnknownTlbConstructorException
import org.ton.tlb.loadTlb
import java.time.Instant

@Component
class BlockJob(
    private val blockIdServiceProperties: BlockIdServiceProperties,
    private val scheduler: Scheduler,
    private val liteClient: LiteClient,
    private val blockIdRepository: BlockIdRepository,
    private val exchangePairRepository: ExchangePairRepository,
    private val swapRepository: SwapRepository,
    private val tokenRepository: TokenRepository,
) : Job, CoroutineScope by CoroutineScope(Dispatchers.IO + CoroutineName("BlockJob")) {
    override fun execute(context: JobExecutionContext) {
        val jobData = context.mergedJobDataMap
        val blockIdExt = jobData["blockIdExt"] as TonNodeBlockIdExt

        if (blockIdRepository.existsByWorkchainAndShardAndSeqno(
                blockIdExt.workchain,
                blockIdExt.shard,
                blockIdExt.seqno
            )
        )
            return

        launch {
            val block = try {
                liteClient.getBlock(blockIdExt) // TODO: Retries
            } catch (e: Exception) {
                logger.warn(e) { "couldn't get masterchain block seqno=${blockIdExt.seqno}" }
                null
            } ?: return@launch

            logger.debug { "workchain=${blockIdExt.workchain} block seqno=${blockIdExt.seqno}" }
            val blockId = withContext(Dispatchers.IO) {
                if (!blockIdRepository.existsByWorkchainAndShardAndSeqno(
                        blockIdExt.workchain,
                        blockIdExt.shard,
                        blockIdExt.seqno
                    )
                ) {
                    try {
                        blockIdRepository.save(
                            BlockId(
                                blockIdExt,
                                Instant.ofEpochSecond(block.info.gen_utime.toLong())
                            )
                        )
                    } catch (e: Exception) {
                        logger.warn(e) { "Failed to save block id" }
                        null
                    }
                } else {
                    null
                }
            } ?: return@launch

            // Process shardchain blocks
            if (blockId.workchain == -1) {
                block.extra.custom.value?.shard_hashes
                    ?.nodes()
                    .orEmpty()
                    .associate { BigInt(it.first.toByteArray()).toInt() to it.second.nodes().maxBy { it.seq_no } }
                    .map { (workchain, shard) ->
                        TonNodeBlockId(
                            workchain,
                            Shard.ID_ALL, // shard.next_validator_shard.toLong(),
                            shard.seq_no.toInt(),
                        )
                    }
                    .filterNot { blockIdRepository.existsByWorkchainAndShardAndSeqno(it.workchain, it.shard, it.seqno) }
                    .forEach {
                        val shardchainBlockIdJobKey =
                            JobKey.jobKey("LookupBlockIdJob_${it.workchain}:${it.shard}:${it.seqno}")
                        if (!scheduler.checkExists(shardchainBlockIdJobKey)) {
                            scheduler.scheduleJob(
                                JobBuilder.newJob(LookupBlockIdJob::class.java)
                                    .withIdentity(shardchainBlockIdJobKey)
                                    .usingJobData(JobDataMap(mapOf("blockId" to it)))
                                    .build(),
                                TriggerBuilder.newTrigger()
                                    .withIdentity("LookupBlockIdJob_${it.workchain}:${it.shard}:${it.seqno}")
                                    .startNow()
                                    .build()
                            )
                        }
                    }
            }

            if (
                ((blockId.workchain == -1 && blockId.seqno > blockIdServiceProperties.genesisMasterchainSeqno) ||
                        ((block.info.master_ref?.master?.seq_no
                            ?: UInt.MAX_VALUE) > blockIdServiceProperties.genesisMasterchainSeqno.toUInt()))
                && withContext(
                    Dispatchers.IO
                ) {
                    !blockIdRepository.existsByWorkchainAndShardAndSeqno(
                        blockId.workchain,
                        blockId.shard,
                        blockId.seqno - 1
                    )
                }
            ) { // Process previous blocks
                val shardchainBlockIdJobKey =
                    JobKey.jobKey("LookupBlockIdJob_${blockId.workchain}:${blockId.shard}:${blockId.seqno - 1}")
                if (!scheduler.checkExists(shardchainBlockIdJobKey)) {
                    scheduler.scheduleJob(
                        JobBuilder.newJob(LookupBlockIdJob::class.java)
                            .withIdentity(shardchainBlockIdJobKey)
                            .usingJobData(
                                JobDataMap(
                                    mapOf(
                                        "blockId" to TonNodeBlockId(blockId.workchain, blockId.shard, blockId.seqno - 1)
                                    )
                                )
                            )
                            .build(),
                        TriggerBuilder.newTrigger()
                            .withIdentity("LookupBlockIdTrigger_${blockId.workchain}:${blockId.shard}:${blockId.seqno - 1}")
                            .startNow()
                            .build()
                    )
                }
            }

            // Process transactions
            block.extra.account_blocks.nodes()
                .flatMap { (account, _) ->
                    account.transactions.nodes().map { (transaction, _) -> blockId to transaction }
                }
                .forEach {
                    processTransaction(it.first, it.second)
                }
        }
    }

    private fun processTransaction(blockId: BlockId, transaction: Transaction) {
        // Monitor tokens as well
        if (tokenRepository.existsByAddress(AddrStd(blockId.workchain, transaction.account_addr))) {
            val tokenJobKey = JobKey(
                "TokenJob_${AddrStd(blockId.workchain, transaction.account_addr).toSafeString()}_${blockId.id}",
                "TokenJob"
            )
            if (!scheduler.checkExists(tokenJobKey)) {
                scheduler.scheduleJob(
                    JobBuilder.newJob(TokenJob::class.java)
                        .withIdentity(tokenJobKey)
                        .usingJobData(
                            JobDataMap(
                                mapOf(
                                    "address" to AddrStd(blockId.workchain, transaction.account_addr),
                                    "blockId" to blockId
                                )
                            )
                        )
                        .build(),
                    TriggerBuilder.newTrigger()
                        .withIdentity(
                            "TokenJobTrigger_${
                                AddrStd(
                                    blockId.workchain,
                                    transaction.account_addr
                                ).toSafeString()
                            }_${blockId.id}", "TokenJob"
                        )
                        .startNow()
                        .build()
                )
            }
        }

        // Only interested in internal messages
        if (!(transaction.in_msg.value?.info is IntMsgInfo && transaction.out_msgs.firstOrNull()?.second?.info is IntMsgInfo))
            return


        // Only interested in exchange pair contracts
        if (!exchangePairRepository.existsByAddress(AddrStd(blockId.workchain, transaction.account_addr)))
            return

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
            swapRepository.save(
                Swap(
                    destination = outMsgOp.destination,
                    baseAmount = inMsgInfo.value.coins.amount.value - outMsgInfo.value.coins.amount.value,
                    exchangePairAddress = exchangePair,
                    quoteAmount = outMsgOp.amount.value,
                    inverse = false, // Base (TON) -> Quote (Token) swap
                    referrer = parseOpReferrer(inMsgOp.params.customPayload),
                    queryId = outMsgOp.queryId,
                    transaction = transaction,
                    blockId = blockId,
                    timestamp = Instant.now(),
                )
                    .apply {
                        this.exchangePair = exchangePairRepository.findByAddress(exchangePair).orElse(null)
                    }
            )
                .also { logger.debug { "${it.queryId}: ${it.baseAmount} TON -> ${it.quoteAmount} Jetton" } }
        } else if (inMsgOp is TransferNotificationOp && inMsgOp.forwardPayloadOp() is SwapJettonPayloadOp && outMsgOp is SuccessfulSwapOp) {
            // Successful Jetton -> TON swap
            swapRepository.save(
                Swap(
                    destination = outMsgInfo.dest,
                    baseAmount = outMsgInfo.value.coins.amount.value - inMsgInfo.value.coins.amount.value,
                    exchangePairAddress = exchangePair,
                    quoteAmount = inMsgOp.amount.value,
                    inverse = true, // Quote (Token) -> Base (TON) swap
                    referrer = parseOpReferrer((inMsgOp.forwardPayloadOp() as SwapJettonPayloadOp).params.customPayload),
                    queryId = outMsgOp.queryId,
                    transaction = transaction,
                    blockId = blockId,
                    timestamp = Instant.now(),
                )
                    .apply {
                        this.exchangePair = exchangePairRepository.findByAddress(exchangePair).orElse(null)
                    }
            )
                .also { logger.debug { "${it.queryId}: ${it.quoteAmount} Jetton -> ${it.baseAmount} TON" } }
        } else if (inMsgOp is TransferNotificationOp && inMsgOp.forwardPayloadOp() is AddLiquidityPayloadOp && outMsgOp is TransferOp) {
            // Successful liquidity deposit
            val liquidityJobKey =
                JobKey(
                    "LiquidityJob_${inMsgOp.sender.toSafeString()}_${exchangePair.toSafeString()}_${blockId.id}",
                    "LiquidityJob"
                )

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
                            "LiquidityTrigger_${inMsgOp.sender.toSafeString()}_${exchangePair.toSafeString()}_${blockId.id}",
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
                            "LiquidityTrigger_${inMsgOp.sender.toSafeString()}_${exchangePair.toSafeString()}_${blockId.id}",
                            "LiquidityTrigger"
                        )
                        .startNow()
                        .build()
                )
        } else {
            logger.warn { "$blockId: unhandled $inMsgOp -> $outMsgOp" }
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
