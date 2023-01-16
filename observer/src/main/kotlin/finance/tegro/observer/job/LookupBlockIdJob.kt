package finance.tegro.observer.job

import finance.tegro.core.repository.BlockIdRepository
import kotlinx.coroutines.*
import mu.KLogging
import org.quartz.*
import org.quartz.Job
import org.springframework.stereotype.Component
import org.ton.api.tonnode.TonNodeBlockId
import org.ton.lite.client.LiteClient

@Component
class LookupBlockIdJob(
    private val scheduler: Scheduler,
    private val liteClient: LiteClient,
    private val blockIdRepository: BlockIdRepository,
) : Job, CoroutineScope by CoroutineScope(Dispatchers.IO + CoroutineName("LookupBlockIdJob")) {
    override fun execute(context: JobExecutionContext) {
        val jobData = context.mergedJobDataMap
        val blockId = jobData["blockId"] as TonNodeBlockId

        if (blockIdRepository.existsByWorkchainAndSeqno(blockId.workchain, blockId.seqno))
            return

        launch {
            val blockIdExt = try {
                liteClient.lookupBlock(blockId) // TODO: Retries
            } catch (e: Exception) {
                logger.warn(e) { "couldn't get shardchain block id workchain=${blockId.workchain} seqno=${blockId.seqno}" }
                null
            } ?: return@launch


            if (withContext(Dispatchers.IO) {
                    blockIdRepository.existsByWorkchainAndShardAndSeqno(
                        blockIdExt.workchain,
                        blockIdExt.shard,
                        blockIdExt.seqno
                    )
                })
                return@launch

            logger.trace { "workchain=${blockIdExt.workchain} block seqno=${blockIdExt.seqno}" }

            val blockJobKey =
                JobKey("BlockJob_${blockIdExt.workchain}:${blockIdExt.shard}:${blockIdExt.seqno}", "BlockJob")

            if (!scheduler.checkExists(blockJobKey)) {
                scheduler.scheduleJob(
                    JobBuilder.newJob(BlockJob::class.java)
                        .withIdentity(blockJobKey)
                        .usingJobData(JobDataMap(mapOf("blockIdExt" to blockIdExt)))
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
