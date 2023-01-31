package finance.tegro.tonindexer.db

import com.github.benmanes.caffeine.cache.Caffeine
import finance.tegro.tonindexer.ydb.Ydb
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.block.Block
import tech.ydb.table.query.Params
import tech.ydb.table.values.PrimitiveValue

interface BlockStorage {
    suspend fun saveBlocks(blockId: TonNodeBlockIdExt, block: Block)

    suspend fun contains(blockId: TonNodeBlockIdExt): Boolean
}

class BlockStorageYdb : BlockStorage {
    private val saveBlocksQueue = Channel<Pair<TonNodeBlockIdExt, Block>>(Channel.UNLIMITED)
    private val job = GlobalScope.launch {
        delay(5000)
        while (true) {
            saveBlocksJob()
            delay(5000)
        }
    }

    //26990079	9223372036854775808	2023-01-31T14:31:43Z	-1
    private val cache = Caffeine.newBuilder()
        .maximumSize(100)
        .build<TonNodeBlockIdExt, Unit>()

    override suspend fun saveBlocks(blockId: TonNodeBlockIdExt, block: Block) {
        cache.put(blockId, Unit)
        saveBlocksQueue.send(blockId to block)
    }

    override suspend fun contains(blockId: TonNodeBlockIdExt): Boolean {
        return cache.getIfPresent(blockId) != null || Ydb.dataQuery(
            "" +
                    "DECLARE \$root_hash as STRING;" +
                    "SELECT root_hash from ton_blocks WHERE root_hash = \$root_hash;", Params.of(
                "\$root_hash", PrimitiveValue.newBytes(blockId.rootHash.toByteArray())
            )
        ).getResultSet(0).rowCount != 0
    }

    private suspend fun saveBlocksJob() {
        val blocks = ArrayList<Pair<TonNodeBlockIdExt, Block>>()
        blocks.add(saveBlocksQueue.receive())
        while (true) {
            val blockPair = saveBlocksQueue.tryReceive().getOrNull() ?: break
            blocks.add(blockPair)
        }

        val query = StringBuilder()
        query.append(
            """
            INSERT INTO ton_blocks (
            workchain, shard, seqno, root_hash, file_hash, time
            ) VALUES 
        """.trimIndent()
        )

        blocks.map { (blockId, block) ->
            val blockInfo = block.info.value
            val time = blockInfo.genUtime

            """ (
            ${blockId.workchain},
            ${blockId.shard.toULong()},
            ${blockId.seqno},
            "${blockId.rootHash}",
            "${blockId.fileHash}",
            CAST($time AS Datetime)
            )""".trimIndent()
        }.joinToString(",").forEach {
            query.append(it)
        }
        query.append(";")
        println(query)
        Ydb.dataQuery(query.toString())
        println("SAVED: ${blocks.size}")
    }
}
