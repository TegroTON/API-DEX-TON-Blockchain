package finance.tegro.tonindexer.services

import com.github.benmanes.caffeine.cache.Caffeine
import finance.tegro.tonindexer.db.BlockStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import org.slf4j.LoggerFactory
import org.ton.api.tonnode.Shard
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.bigint.BigInt
import org.ton.bitstring.BitString
import org.ton.block.Block
import org.ton.block.PrevBlkInfo
import org.ton.block.PrevBlksInfo
import org.ton.block.ShardDescrNew
import org.ton.boc.BagOfCells
import org.ton.lite.api.liteserver.functions.LiteServerGetBlock
import kotlin.coroutines.CoroutineContext

object BlockSynchronizerService : CoroutineScope {
    private val log = LoggerFactory.getLogger(BlockSynchronizerService::class.java)

    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineName(log.name)

    private var job: Job? = null
    private lateinit var blockStorage: BlockStorage
    private val cache = Caffeine.newBuilder()
        .maximumSize(1000)
        .build<BitString, Unit>()

    fun init(blockStorage: BlockStorage) {
        this.blockStorage = blockStorage
        job?.cancel()
        job = subscribeBlockJob()
    }

    private fun subscribeBlockJob() = launch {
        MasterchainBlockService.blockIdFlow.collectLatest { mcBlockId ->
            processBlock(mcBlockId)
            error("done.")
        }
    }

    private suspend fun processBlock(id: TonNodeBlockIdExt): Unit = coroutineScope {
        if (cache.getIfPresent(id.rootHash) != null) {
            log.info("Block already processed: $id")
            return@coroutineScope
        } else {
            cache.put(id.rootHash, Unit)
        }
        val block = downloadBlock(id)
        val blockInfo = block.info.value
        log.info("Start processing block: $id")
        if (id.workchain == -1) {
            block.extra.value.custom.value?.value?.shardHashes?.nodes().orEmpty()
                .associate { (workchain, shard) ->
                    BigInt(workchain.toByteArray()).toInt() to shard.nodes().filterIsInstance<ShardDescrNew>()
                        .maxBy { it.seqNo }
                }
                .map { (workchain, shard) ->
                    TonNodeBlockIdExt(workchain, Shard.ID_ALL, shard.seqNo.toInt(), shard.rootHash, shard.fileHash)
                }
                .filter { cache.getIfPresent(it.rootHash) == null }
                .map { shardBlockId ->
                    async {
                        processBlock(shardBlockId)
                    }
                }.awaitAll()
        } else {
            val prevBlkInfos = when (val prevBlkInfo = blockInfo.prevRef.value) {
                is PrevBlkInfo -> listOf(prevBlkInfo.prev)
                is PrevBlksInfo -> listOf(prevBlkInfo.prev1.value, prevBlkInfo.prev2.value)
            }
            val prevBlockIds = prevBlkInfos.map { prevBlkInfo ->
                TonNodeBlockIdExt(
                    blockInfo.shard.workchainId,
                    blockInfo.shard.shardPrefix.toLong() or (1L shl (63 - blockInfo.shard.shardPfxBits)),
                    prevBlkInfo.seqNo.toInt(),
                    prevBlkInfo.rootHash,
                    prevBlkInfo.fileHash
                )
            }
            log.info("Prev blocks processing: ${prevBlockIds.size}")
            prevBlockIds.map { prevBlockId ->
                async {
                    processBlock(prevBlockId)
                }
            }.awaitAll()
            log.info("Prev blocks processing done")
        }
        log.info("End process block: $id")
    }
//
//    private suspend fun saveToDb(blockId: TonNodeBlockIdExt, block: Block) {

//    }

    private suspend fun downloadBlock(blockId: TonNodeBlockIdExt): Block {
        val blockData = TonLiteApiService.liteApi(LiteServerGetBlock(blockId))
        return Block.loadTlb(BagOfCells(blockData.data).first())
    }
}
