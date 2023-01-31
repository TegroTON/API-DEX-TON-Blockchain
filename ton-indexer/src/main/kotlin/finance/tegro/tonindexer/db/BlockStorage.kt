package finance.tegro.tonindexer.db

import finance.tegro.tonindexer.ydb.Ydb
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.block.Block
import tech.ydb.table.values.PrimitiveValue

interface BlockStorage {
    suspend fun saveBlock(blockId: TonNodeBlockIdExt, block: Block)
}

class BlockStorageYdb : BlockStorage {
    override suspend fun saveBlock(blockId: TonNodeBlockIdExt, block: Block) {
        val query = "UPSERT INTO blocks (workchain, shard, seqno, root_hash, file_hash) " +
                "VALUES (${blockId.workchain},${blockId.shard.toULong()},${blockId.seqno}, " +
                "${PrimitiveValue.newBytes(blockId.rootHash.toByteArray())}, " +
                "${PrimitiveValue.newBytes(blockId.fileHash.toByteArray())});"
        Ydb.dataQuery(query)
    }
}
