package finance.tegro.observer.job

import finance.tegro.core.repository.BlockIdRepository
import finance.tegro.observer.properties.BlockIdServiceProperties
import org.quartz.*
import org.springframework.stereotype.Component
import org.ton.api.tonnode.Shard
import org.ton.api.tonnode.TonNodeBlockId

@Component
class CatchUpMissingBlockJob(
    private val scheduler: Scheduler,
    private val blockIdRepository: BlockIdRepository,
    private val blockIdServiceProperties: BlockIdServiceProperties,
) : Job {
    override fun execute(context: JobExecutionContext) {
        for (lastMissedBlockSeqno in
        blockIdRepository.findMissingSeqnos(-1, blockIdServiceProperties.genesisMasterchainSeqno, 128)) {
            val lookupBlockIdJobKey = JobKey(
                "LookupBlockIdJob_-1:${Shard.ID_ALL}:${lastMissedBlockSeqno}",
                "LookupBlockIdJob"
            )
            if (!scheduler.checkExists(lookupBlockIdJobKey)) {
                scheduler.scheduleJob(
                    JobBuilder.newJob(LookupBlockIdJob::class.java)
                        .withIdentity(lookupBlockIdJobKey)
                        .usingJobData(
                            JobDataMap(
                                mapOf(
                                    "blockId" to TonNodeBlockId(
                                        -1,
                                        Shard.ID_ALL,
                                        lastMissedBlockSeqno
                                    )
                                )
                            )
                        )
                        .build(),
                    TriggerBuilder.newTrigger()
                        .withIdentity(
                            "LookupBlockIdJobTrigger_-1:${Shard.ID_ALL}:${lastMissedBlockSeqno}",
                            "LookupBlockIdJobTrigger"
                        )
                        .startNow()
                        .build()
                )
            }
        }
    }
}
