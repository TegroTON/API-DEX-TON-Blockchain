package finance.tegro.tonindexer.services

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import org.slf4j.LoggerFactory
import org.ton.api.tonnode.Shard
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.bigint.BigInt
import org.ton.block.*
import org.ton.boc.BagOfCells
import org.ton.lite.api.liteserver.functions.LiteServerGetBlock
import kotlin.coroutines.CoroutineContext

object BlockSubscriberService : CoroutineScope {
    private val log = LoggerFactory.getLogger(BlockSubscriberService::class.java)

    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineName(log.name)

    private var job: Job? = null
    private val mcBlockQueue = Channel<TonNodeBlockIdExt>()
    private val maxBasechainId = atomic<TonNodeBlockIdExt?>(null)

    private val masterchainBlockFlow = MutableSharedFlow<Block>()
    private val basechainBlockFlow = MutableSharedFlow<Block>()

    fun init() {
        job?.cancel()
        job = launch {
            subscribeBlockJob()
            handleMasterchainBlockJob()
        }
    }

    fun blockFlow(workchain: Int): SharedFlow<Block> {
        return when (workchain) {
            -1 -> masterchainBlockFlow.asSharedFlow()
            0 -> basechainBlockFlow.asSharedFlow()
            else -> throw UnsupportedOperationException("Unsupported workchain: $workchain")
        }
    }

    private fun CoroutineScope.subscribeBlockJob() = launch {
        MasterchainBlockService.blockIdFlow.collectLatest { mcBlockId ->
            mcBlockQueue.send(mcBlockId)
        }
    }

    private fun CoroutineScope.handleMasterchainBlockJob() = launch {
        for (mcBlockId in mcBlockQueue) {
            val block = downloadBlock(mcBlockId)
            processMasterchainBlock(block)
        }
    }

    private suspend fun processMasterchainBlock(block: Block): Unit = coroutineScope {
        val blockInfo = block.info.value
        val shardIdent = blockInfo.shard
        val workchain = shardIdent.workchainId
        val shard = shardIdent.shardPrefix.toLong() or (1L shl (63 - shardIdent.shardPfxBits))
        require(workchain == -1)
        masterchainBlockFlow.emit(block)
        block.extra.value.custom.value!!.value.shardHashes.nodes()
            .associate { (workchain, shard) ->
                BigInt(workchain.toByteArray()).toInt() to shard
            }.map { (workchain, shardDescrTree) ->
                launch {
                    val shards = processShard(shardDescrTree, shard)
                    val shardBlocks = shards.map { (shard, shardDescr) ->
                        TonNodeBlockIdExt(
                            workchain,
                            shard,
                            shardDescr.seqNo.toInt(),
                            shardDescr.rootHash,
                            shardDescr.fileHash
                        )
                    }.toSortedSet { o1, o2 -> o1.seqno.compareTo(o2.seqno) }
                    val maxBasechainId = shardBlocks.last()
                    val minBasechainId = this@BlockSubscriberService.maxBasechainId.value
                    val seqnoSequence =
                        maxBasechainId.seqno downTo (minBasechainId?.seqno?.plus(1) ?: (maxBasechainId.seqno))
                    val knownWorkchainBlocks = shardBlocks.associateBy { it.seqno }.toMutableMap()
                    val workchainBlocks = HashMap<Int, Block>()
                    seqnoSequence.forEach { seqno ->
//                        println("block $seqno")
                        if (seqno !in seqnoSequence) return@forEach
                        val blockId = knownWorkchainBlocks[seqno]
                        if (blockId != null) {
                            val workchainBlock = downloadBlock(blockId)
                            workchainBlocks[seqno] = workchainBlock
                        } else {
                            val prevRefs = when (val prevRef = workchainBlocks[seqno + 1]!!.info.value.prevRef.value) {
                                is PrevBlkInfo -> listOf(prevRef.prev)
                                is PrevBlksInfo -> listOf(prevRef.prev2.value, prevRef.prev1.value)
                            }
                            prevRefs.mapNotNull { prevRef ->
                                if (prevRef.seqNo.toInt() in seqnoSequence) {
                                    async {
                                        downloadBlock(
                                            TonNodeBlockIdExt(
                                                workchain,
                                                shard,
                                                prevRef.seqNo.toInt(),
                                                prevRef.rootHash,
                                                prevRef.fileHash
                                            )
                                        )
                                    }
                                } else null
                            }.awaitAll().forEach {
                                workchainBlocks[it.info.value.seqNo] = it
                            }
                        }
                    }
                    seqnoSequence.reversed().forEach { seqno ->
                        val workchainBlock = workchainBlocks[seqno]!!
                        basechainBlockFlow.emit(workchainBlock)
                    }
                    this@BlockSubscriberService.maxBasechainId.value = maxBasechainId
                }
            }.joinAll()
    }

    private fun processShard(
        binTree: BinTree<ShardDescr>,
        shard: Long = Shard.ID_ALL
    ): Sequence<Pair<Long, ShardDescrNew>> =
        when (binTree) {
            is BinTreeFork -> {
                val x = ((shard and (shard.inv() + 1)) ushr 1)
                sequence {
                    yieldAll(processShard(binTree.left.value, shard - x))
                    yieldAll(processShard(binTree.right.value, shard + x))
                }
            }

            is BinTreeLeaf -> sequenceOf(shard to (binTree.leaf as ShardDescrNew))
        }

    private suspend fun downloadBlock(blockId: TonNodeBlockIdExt): Block {
        val blockData = TonLiteApiService.liteApi(LiteServerGetBlock(blockId))
        return Block.loadTlb(BagOfCells(blockData.data).first())
    }
}
