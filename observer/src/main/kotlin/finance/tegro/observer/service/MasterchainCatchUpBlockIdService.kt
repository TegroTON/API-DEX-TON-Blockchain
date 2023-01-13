package finance.tegro.observer.service

import finance.tegro.core.entity.BlockId
import finance.tegro.core.repository.BlockIdRepository
import finance.tegro.observer.properties.BlockIdServiceProperties
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.time.delay
import org.springframework.stereotype.Service
import org.ton.api.tonnode.Shard
import org.ton.api.tonnode.TonNodeBlockId
import org.ton.lite.client.LiteClient

@Service
class MasterchainCatchUpBlockIdService(
    private val liteClient: LiteClient,

    private val blockIdRepository: BlockIdRepository,
    private val blockIdServiceProperties: BlockIdServiceProperties,
) {
    val data = flow {
        while (currentCoroutineContext().isActive) {
            blockIdRepository.findMissingSeqnos(
                -1,
                blockIdServiceProperties.catchUpStartSeqno,
                blockIdServiceProperties.catchUpStep
            )
                .forEach { emit(it) }

            delay(blockIdServiceProperties.catchUpRate)
        }
    }
        .distinctUntilChanged()
        .map { TonNodeBlockId(-1, Shard.ID_ALL, it) }
        .mapNotNull { id ->
            try {
                liteClient.lookupBlock(id) // TODO: Retries
            } catch (e: Exception) {
                ShardchainBlockIdService.logger.warn(e) { "couldn't get old masterchain block id seqno=${id.seqno}" }
                null
            }
        }
        .map { blockIdRepository.save(BlockId(it)) }
        .flowOn(Dispatchers.IO)
        .onEach { ShardchainBlockIdService.logger.trace { "latest workchain=${it.workchain} block seqno=${it.seqno}" } }
        .shareIn(
            CoroutineScope(Dispatchers.IO + CoroutineName("MasterchainCatchUpBlockIdService")),
            SharingStarted.Eagerly,
            256
        )

}
