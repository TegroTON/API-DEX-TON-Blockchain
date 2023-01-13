package finance.tegro.observer.service

import finance.tegro.core.entity.BlockId
import finance.tegro.core.repository.BlockIdRepository
import finance.tegro.observer.properties.BlockIdServiceProperties
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.time.delay
import mu.KLogging
import org.springframework.stereotype.Service
import org.ton.lite.client.LiteClient

@Service
class MasterchainBlockIdService(
    private val blockIdServiceProperties: BlockIdServiceProperties,

    private val liteClient: LiteClient,
    private val blockIdRepository: BlockIdRepository,
) {
    val data = flow {
        while (currentCoroutineContext().isActive) {
            try {
                emit(liteClient.getLastBlockId())
            } catch (e: Exception) {
                logger.error(e) { "Failed to get latest block id" }
            }
            delay(blockIdServiceProperties.pollRate)
        }
    }
        .distinctUntilChanged()
        .filterNot { blockIdRepository.existsByWorkchainAndShardAndSeqno(it.workchain, it.shard, it.seqno) }
        .map { blockIdRepository.save(BlockId(it)) }
        .flowOn(Dispatchers.IO)
        .onEach { logger.trace { "latest masterchain block seqno=${it.seqno}" } }
        .shareIn(
            CoroutineScope(Dispatchers.IO + CoroutineName("MasterchainBlockIdService")),
            SharingStarted.Eagerly,
            256
        )

    companion object : KLogging()
}
