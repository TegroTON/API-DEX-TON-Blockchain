package finance.tegro.observer.service

import finance.tegro.core.repository.BlockIdRepository
import finance.tegro.observer.properties.BlockIdServiceProperties
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.time.delay
import mu.KLogging
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
                logger.warn(e) { "couldn't get old masterchain block id seqno=${id.seqno}" }
                null
            }
        }
        .filterNot { blockIdRepository.existsByWorkchainAndShardAndSeqno(it.workchain, it.shard, it.seqno) }
        .flowOn(Dispatchers.IO)
        .onEach { logger.trace { "old masterchain block id seqno=${it.seqno}" } }
        .shareIn(
            CoroutineScope(Dispatchers.IO + CoroutineName("MasterchainCatchUpBlockIdService")),
            SharingStarted.Eagerly,
            256
        )

    companion object : KLogging()
}
