package finance.tegro.tonindexer.services

import finance.tegro.tonindexer.ydb.Ydb
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import org.slf4j.LoggerFactory
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.block.Block
import org.ton.boc.BagOfCells
import org.ton.lite.api.liteserver.functions.LiteServerGetBlock
import tech.ydb.table.values.PrimitiveValue
import kotlin.coroutines.CoroutineContext

object BlockSynchronizerService : CoroutineScope {
    private val log = LoggerFactory.getLogger(BlockSynchronizerService::class.java)

    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineName(log.name)

    private var job: Job? = null

    fun init(

    ) {
        job?.cancel()
        job = subscribeBlockJob()
    }

    private fun subscribeBlockJob() = launch {
        MasterchainBlockService.blockIdFlow.collectLatest { mcBlockId ->
            val mcBlock = downloadBlock(mcBlockId)

        }
    }


    private suspend fun saveToDb(blockId: TonNodeBlockIdExt, block: Block) {
        val query = "UPSERT INTO blocks (workchain, shard, seqno, root_hash, file_hash) " +
                "VALUES (${blockId.workchain},${blockId.shard.toULong()},${blockId.seqno}, " +
                "${PrimitiveValue.newBytes(blockId.rootHash.toByteArray())}, " +
                "${PrimitiveValue.newBytes(blockId.fileHash.toByteArray())});"
        Ydb.dataQuery(query)
    }

    private suspend fun downloadBlock(blockId: TonNodeBlockIdExt): Block {
        val blockData = TonLiteApiService.liteApi(LiteServerGetBlock(blockId))
        return Block.loadTlb(BagOfCells(blockData.data).first())
    }
}
