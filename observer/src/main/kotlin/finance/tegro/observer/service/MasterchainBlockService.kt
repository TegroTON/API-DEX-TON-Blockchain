package finance.tegro.observer.service

import finance.tegro.core.repository.BlockIdRepository
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withContext
import mu.KLogging
import org.springframework.stereotype.Service
import org.ton.lite.client.LiteClient
import java.time.Instant

@Service
class MasterchainBlockService(
    private val masterchainBlockIdService: MasterchainBlockIdService,
    private val liteClient: LiteClient,
    private val blockIdRepository: BlockIdRepository,
) {
    val data = masterchainBlockIdService.data
        .mapNotNull { id ->
            try {
                liteClient.getBlock(id.toTonNodeBlockIdExt())?.let { id to it } // TODO: Retries
            } catch (e: Exception) {
                logger.warn(e) { "couldn't get masterchain block seqno=${id.seqno}" }
                null
            }
        }
        .onEach { (id, block) ->
            logger.debug { "masterchain block seqno=${id.seqno}" }
            withContext(Dispatchers.IO) {
                blockIdRepository.updateTimestampByWorkchainAndShardAndSeqno(
                    Instant.ofEpochSecond(block.info.gen_utime.toLong()),
                    id.workchain,
                    id.shard,
                    id.seqno
                )
            }
        }
        .shareIn(CoroutineScope(Dispatchers.IO + CoroutineName("MasterchainBlockService")), SharingStarted.Eagerly, 256)

    companion object : KLogging()
}
