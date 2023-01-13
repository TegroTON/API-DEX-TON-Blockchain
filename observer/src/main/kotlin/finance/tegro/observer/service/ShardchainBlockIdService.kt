package finance.tegro.observer.service

import finance.tegro.core.entity.BlockId
import finance.tegro.core.repository.BlockIdRepository
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import mu.KLogging
import org.springframework.stereotype.Service
import org.ton.api.tonnode.Shard
import org.ton.api.tonnode.TonNodeBlockId
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.bigint.BigInt
import org.ton.block.Block
import org.ton.block.ShardDescr
import org.ton.lite.client.LiteClient
import java.util.concurrent.ConcurrentHashMap

@Service
class ShardchainBlockIdService(
    private val blockIdRepository: BlockIdRepository,
    private val masterchainBlockService: MasterchainBlockService,
    private val liteClient: LiteClient,
) {
    @OptIn(FlowPreview::class)
    val data = masterchainBlockService.data
        .flatMapConcat(::getShardchainBlockIds)
        .distinctUntilChanged()
        .filterNot { blockIdRepository.existsByWorkchainAndShardAndSeqno(it.workchain, it.shard, it.seqno) }
        .map { blockIdRepository.save(BlockId(it)) }
        .flowOn(Dispatchers.IO)
        .onEach { logger.trace { "latest workchain=${it.workchain} block seqno=${it.seqno}" } }
        .shareIn(
            CoroutineScope(Dispatchers.IO + CoroutineName("ShardchainBlockIdService")),
            SharingStarted.Eagerly,
            256
        )

    private val lastMasterchainShards = ConcurrentHashMap<Int, ShardDescr>()

    private suspend fun getShardchainBlockIds(
        masterchainBlockIdAndBlock: Pair<BlockId, Block>,
    ): Flow<TonNodeBlockIdExt> {
        val (_, masterchainBlock) = masterchainBlockIdAndBlock

        val masterchainShards = masterchainBlock.extra.custom.value?.shard_hashes
            ?.nodes()
            .orEmpty()
            .associate { BigInt(it.first.toByteArray()).toInt() to it.second.nodes().maxBy { it.seq_no } }

        val shardchainBlockIds = masterchainShards
            .flatMap { (workchain, shard) ->
                (lastMasterchainShards.getOrDefault(workchain, shard).seq_no + 1u..shard.seq_no)
                    .map { seqno ->
                        TonNodeBlockId(
                            workchain,
                            Shard.ID_ALL, // shard.next_validator_shard.toLong(),
                            seqno.toInt(),
                        )
                    }
            }
            .asFlow()
            .mapNotNull { id ->
                try {
                    liteClient.lookupBlock(id) // TODO: Retries
                } catch (e: Exception) {
                    logger.warn(e) { "couldn't get shardchain block id workchain=${id.workchain} seqno=${id.seqno}" }
                    null
                }
            }

        lastMasterchainShards.clear()
        lastMasterchainShards.putAll(masterchainShards)

        return shardchainBlockIds
    }

    companion object : KLogging()
}
