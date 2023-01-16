package finance.tegro.observer.job

import finance.tegro.core.repository.BlockIdRepository
import kotlinx.coroutines.*
import mu.KLogging
import org.quartz.*
import org.quartz.Job
import org.springframework.stereotype.Component
import org.ton.lite.client.LiteClient

@Component
class MasterchainBlockIdJob(
    private val scheduler: Scheduler,
    private val liteClient: LiteClient,
    private val blockIdRepository: BlockIdRepository,
) : Job, CoroutineScope by CoroutineScope(Dispatchers.IO + CoroutineName("MasterchainBlockIdJob")) {
    override fun execute(context: JobExecutionContext) {
        launch {
            val blockId = try {
                liteClient.getLastBlockId()
            } catch (e: Exception) {
                logger.error(e) { "Failed to get latest block id" }
                return@launch
            }

            if (withContext(Dispatchers.IO) {
                    blockIdRepository.existsByWorkchainAndShardAndSeqno(
                        blockId.workchain,
                        blockId.shard,
                        blockId.seqno
                    )
                })
                return@launch

            logger.trace { "latest masterchain block seqno=${blockId.seqno}" }

            val blockJobKey = JobKey("BlockJob_${blockId.workchain}:${blockId.shard}:${blockId.seqno}", "BlockJob")

            if (!scheduler.checkExists(blockJobKey)) {
                scheduler.scheduleJob(
                    JobBuilder.newJob(BlockJob::class.java)
                        .withIdentity(blockJobKey)
                        .usingJobData(JobDataMap(mapOf("blockIdExt" to blockId)))
                        .build(),
                    TriggerBuilder.newTrigger()
                        .withIdentity(
                            "BlockJobTrigger_${blockId.workchain}:${blockId.shard}:${blockId.seqno}", "BlockJobTrigger"
                        )
                        .startNow()
                        .build()
                )
            }
        }
    }

    companion object : KLogging()
}
