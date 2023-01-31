package finance.tegro.tonindexer

import finance.tegro.tonindexer.db.BlockStorageYdb
import finance.tegro.tonindexer.services.BlockSynchronizerService
import finance.tegro.tonindexer.services.MasterchainBlockService
import finance.tegro.tonindexer.services.TonLiteApiService

suspend fun main(args: Array<String>) {
//    Ydb.init()
    TonLiteApiService.init()
    MasterchainBlockService.init()
    BlockSynchronizerService.init(BlockStorageYdb())

    while (true) {

    }
}
