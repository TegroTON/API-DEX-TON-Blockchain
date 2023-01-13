package finance.tegro.observer.service

import finance.tegro.core.entity.BlockId
import finance.tegro.core.repository.BlockIdRepository
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withContext
import mu.KLogging
import org.springframework.stereotype.Service
import org.ton.lite.client.LiteClient
import java.time.Instant

@Service
class CatchUpShardchainBlockService(
    private val catchUpShardchainBlockIdService: CatchUpShardchainBlockIdService,
    private val liteClient: LiteClient,
    private val blockIdRepository: BlockIdRepository,
) {
    val data = catchUpShardchainBlockIdService.data
        .mapNotNull { id ->
            try {
                liteClient.getBlock(id)?.let { id to it } // TODO: Retries
            } catch (e: Exception) {
                logger.warn(e) { "couldn't get old shardchain workchain=${id.workchain} block seqno=${id.seqno}" }
                null
            }
        }
        .map { (id, block) ->
            logger.debug { "old shardchain workchain=${id.workchain} block seqno=${id.seqno}" }
            withContext(Dispatchers.IO) {
                blockIdRepository.save(
                    BlockId(
                        id,
                        Instant.ofEpochSecond(block.info.gen_utime.toLong())
                    )
                )
            } to block
        }
        .shareIn(CoroutineScope(Dispatchers.IO + CoroutineName("ShardchainBlockService")), SharingStarted.Eagerly, 256)

    companion object : KLogging()
}
