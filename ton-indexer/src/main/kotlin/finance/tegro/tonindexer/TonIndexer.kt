package finance.tegro.tonindexer

import finance.tegro.tonindexer.services.BlockSubscriberService
import finance.tegro.tonindexer.services.MasterchainBlockService
import finance.tegro.tonindexer.services.TonLiteApiService
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

suspend fun main(args: Array<String>): Unit = coroutineScope {
    TonLiteApiService.init()
    MasterchainBlockService.init()
    BlockSubscriberService.init()

    launch {
        BlockSubscriberService.blockFlow(-1).collectLatest { block ->
            val transactions =
                (block.extra.value.inMsgDescr.value.nodes().count() + block.extra.value.outMsgDescr.value.nodes()
                    .count())
            println("-1 block: ${block.info.value.seqNo} - transactions: $transactions")
        }
    }
    launch {
        BlockSubscriberService.blockFlow(0).collectLatest { block ->
            val transactions =
                (block.extra.value.inMsgDescr.value.nodes().count() + block.extra.value.outMsgDescr.value.nodes()
                    .count())
            println(" 0 block: ${block.info.value.seqNo} - transactions: $transactions")
        }
    }
}
