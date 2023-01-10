package finance.tegro.api.service

import finance.tegro.api.contract.SwapParams
import finance.tegro.api.contract.op.*
import finance.tegro.api.entity.Swap
import finance.tegro.api.loadTransaction
import finance.tegro.api.processor.FetchLiquidityProcessor
import finance.tegro.api.processor.FetchReserveProcessor
import finance.tegro.api.processor.FetchTokenContractProcessor
import finance.tegro.api.repository.*
import io.awspring.cloud.messaging.listener.annotation.SqsListener
import mu.KLogging
import org.springframework.messaging.Message
import org.springframework.stereotype.Service
import org.ton.block.*
import org.ton.cell.Cell
import org.ton.lite.api.liteserver.LiteServerTransactionInfo
import org.ton.tlb.exception.UnknownTlbConstructorException
import org.ton.tlb.loadTlb
import java.time.Instant

@Service
class TransactionService(
    private val exchangePairRepository: ExchangePairRepository,
    private val liquidityRepository: LiquidityRepository,
    private val reserveRepository: ReserveRepository,
    private val swapRepository: SwapRepository,
    private val tokenContractRepository: TokenContractRepository,

    private val fetchReserveProcessor: FetchReserveProcessor,
    private val fetchLiquidityProcessor: FetchLiquidityProcessor,
    private val fetchTokenContractProcessor: FetchTokenContractProcessor,
) {
    @SqsListener("transactions-test")
    fun onTransaction(message: Message<LiteServerTransactionInfo>) {
        val transactionInfo = message.payload

        val blockId = transactionInfo.id
        val transaction = transactionInfo.loadTransaction()

        val inMsg = transaction.in_msg.value
        val inMsgInfo = inMsg?.info
        if (inMsgInfo !is IntMsgInfo)
            return // Only interested in internal messages

        if (!exchangePairRepository.existsByAddress(inMsgInfo.dest))
            return // Only interested in transactions to exchange pairs

        val exchangePair = inMsgInfo.dest

        // Update its reserves
        logger.debug { "Updating reserves for $exchangePair" }
        fetchReserveProcessor.process(blockId to exchangePair)
            .let { reserveRepository.save(it) }

        val outMsg = transaction.out_msgs.firstOrNull()?.second
        val outMsgInfo = outMsg?.info
        if (outMsgInfo !is IntMsgInfo)
            return // Only interested in internal messages

        val inMsgOp = parseOp(inMsg.body)?.let {
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
        val outMsgOp = parseOp(outMsg.body)

        logger.debug { "inMsgOp=$inMsgOp -> outMsgOp=$outMsgOp" }

        when (inMsgOp) {
            is SwapTonOp -> {
                if (outMsgOp !is TransferOp)
                    return // Only interested in swaps resulting in out transfers

                val outMsgInnerOp = parsePayloadOp(outMsgOp.forwardPayload)

                if (outMsgInnerOp !is SuccessfulSwapPayloadOp)
                    return // Only interested in successful swaps

                logger.debug { "${outMsgOp.queryId}: swap ${inMsgInfo.value.coins} TON -> ${outMsgOp.amount.value} Jetton" }
                swapRepository.save(
                    Swap(
                        destination = outMsgInfo.dest,
                        baseAmount = inMsgInfo.value.coins.amount.value - outMsgInfo.value.coins.amount.value,
                        exchangePair = exchangePair,
                        quoteAmount = outMsgOp.amount.value,
                        inverse = false, // Base (TON) -> Quote (Token) swap
                        referrer = parseOpReferrer(inMsgOp.params.customPayload),
                        queryId = outMsgOp.queryId,
                        block = blockId,
                        transaction = transaction,
                        timestamp = Instant.now(),
                    )
                )
            }

            is TransferNotificationOp -> {
                val inMsgInnerOp = parsePayloadOp(inMsgOp.forwardPayload)

                when (inMsgInnerOp) {
                    is SwapJettonPayloadOp -> {
                        if (outMsgOp !is SuccessfulSwapOp)
                            return // Only interested in successful swaps

                        logger.debug { "${outMsgOp.queryId}: swap ${inMsgOp.amount.value} Jetton -> ${outMsgInfo.value.coins} TON" }
                        swapRepository.save(
                            Swap(
                                destination = outMsgInfo.dest,
                                baseAmount = outMsgInfo.value.coins.amount.value - inMsgInfo.value.coins.amount.value,
                                exchangePair = exchangePair,
                                quoteAmount = inMsgOp.amount.value,
                                inverse = true, // Quote (Token) -> Base (TON) swap
                                referrer = parseOpReferrer(inMsgInnerOp.params.customPayload),
                                queryId = outMsgOp.queryId,
                                block = blockId,
                                transaction = transaction,
                                timestamp = Instant.now(),
                            )
                        )
                    }

                    is AddLiquidityPayloadOp -> {
                        logger.debug { "processing add liquidity $inMsgInnerOp" }
                        fetchLiquidityProcessor.process(blockId to (inMsgOp.sender to exchangePair))
                            .let { liquidityRepository.save(it) }

                        // Update total supply
                        fetchTokenContractProcessor.process(blockId to exchangePair)
                            .let { tokenContractRepository.save(it) }
                    }

                    else -> return
                }
            }

            is BurnNotificationOp -> {
                logger.debug { "processing burn notification $inMsgOp" }
                fetchLiquidityProcessor.process(blockId to (inMsgOp.sender to exchangePair))
                    .let { liquidityRepository.save(it) }

                // Update total supply
                fetchTokenContractProcessor.process(blockId to exchangePair)
                    .let { tokenContractRepository.save(it) }
            }

            else -> {
                logger.warn { "Unknown input operation $inMsgOp" }
            }
        }
    }

    private fun parseOp(eith: Either<Cell, Cell>?) =
        try {
            eith?.let { it.x ?: it.y }
                ?.parse { loadTlb(Op) }
        } catch (e: UnknownTlbConstructorException) {
            logger.warn(e) { "Unknown tlb constructor" }
            null
        }

    private fun parsePayloadOp(eith: Either<Cell, Cell>?) =
        try {
            eith?.let { it.x ?: it.y }
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
