package finance.tegro.tonindexer

import finance.tegro.tonindexer.services.BlockSubscriberService
import finance.tegro.tonindexer.services.MasterchainBlockService
import finance.tegro.tonindexer.services.TonLiteApiService
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ton.bitstring.Bits256
import kotlin.streams.asStream

suspend fun main(args: Array<String>): Unit = coroutineScope {
    TonLiteApiService.init()
    MasterchainBlockService.init()
    BlockSubscriberService.init()

    launch {
        BlockSubscriberService.blockFlow(-1).collectLatest { block ->
            val transactions =
                (block.extra.value.inMsgDescr.value.nodes().count() + block.extra.value.outMsgDescr.value.nodes()
                    .count())
            block.extra.value.accountBlocks.value.nodes().forEach { (accountBlock, _) ->
                println(" -1:${accountBlock.accountAddr}")
                accountBlock.transactions.forEach { (transactionCell, _) ->
                    println(
                        "    ${
                            Bits256(
                                transactionCell.toCell().hash()
                            )
                        } - ${transactionCell.value.lt} [prev: ${transactionCell.value.prevTransLt}] - ${transactionCell.value.r1.value.inMsg.value?.value?.info?.javaClass?.simpleName}"
                    )
                }
            }
        }
    }
    launch {
        BlockSubscriberService.blockFlow(0).collectLatest { block ->
            val transactionsCount =
                (block.extra.value.inMsgDescr.value.nodes().count() + block.extra.value.outMsgDescr.value.nodes()
                    .count())
            println(" 0 block: ${block.info.value.seqNo} - transactions: $transactionsCount")
            block.extra.value.accountBlocks.value.nodes().forEach { (accountBlock, _) ->
                println("  0:${accountBlock.accountAddr}")
                accountBlock.transactions.forEach { (transactionCell, _) ->
                    println(
                        "    ${
                            Bits256(
                                transactionCell.toCell().hash()
                            )
                        } - ${transactionCell.value.lt} - ${transactionCell.value.r1.value.inMsg.value?.value?.info?.javaClass?.simpleName}"
                    )
                }
            }
        }
    }

    val iterator = listOf(1).iterator()

    iterator.asSequence().asStream()

}
