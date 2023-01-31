package finance.tegro.tonindexer

import finance.tegro.tonindexer.services.BlockSynchronizerService
import finance.tegro.tonindexer.services.MasterchainBlockService
import finance.tegro.tonindexer.services.TonLiteApiService
import finance.tegro.tonindexer.ydb.Ydb

suspend fun main(args: Array<String>) {
    Ydb.init()
    TonLiteApiService.init()
    MasterchainBlockService.init()
    BlockSynchronizerService.init()
}
