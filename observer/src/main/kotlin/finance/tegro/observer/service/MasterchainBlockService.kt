package finance.tegro.observer.service

import finance.tegro.core.entity.BlockId
import finance.tegro.core.repository.BlockIdRepository
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import mu.KLogging
import org.springframework.stereotype.Service
import org.ton.lite.client.LiteClient
import java.time.Instant

@Service
class MasterchainBlockService(
    private val masterchainBlockIdService: MasterchainBlockIdService,
    private val masterchainCatchUpBlockIdService: MasterchainCatchUpBlockIdService,
    private val liteClient: LiteClient,
    private val blockIdRepository: BlockIdRepository,
) {
    val data = merge(masterchainBlockIdService.data, masterchainCatchUpBlockIdService.data)
        .mapNotNull { id ->
            try {
                liteClient.getBlock(id)?.let { id to it } // TODO: Retries
            } catch (e: Exception) {
                logger.warn(e) { "couldn't get masterchain block seqno=${id.seqno}" }
                null
            }
        }
        .map { (id, block) ->
            logger.debug { "masterchain block seqno=${id.seqno}" }
            withContext(Dispatchers.IO) {
                blockIdRepository.save(
                    BlockId(
                        id,
                        Instant.ofEpochSecond(block.info.gen_utime.toLong())
                    )
                )
            } to block
        }
        .shareIn(CoroutineScope(Dispatchers.IO + CoroutineName("MasterchainBlockService")), SharingStarted.Eagerly, 256)

    companion object : KLogging()
}
