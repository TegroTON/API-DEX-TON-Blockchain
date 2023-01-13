package finance.tegro.observer.job

import finance.tegro.core.repository.BlockIdRepository
import finance.tegro.core.repository.ExchangePairRepository
import finance.tegro.core.toSafeString
import mu.KLogging
import org.quartz.*
import org.springframework.stereotype.Component

@Component
class AllReserveJob(
    private val scheduler: Scheduler,

    private val blockIdRepository: BlockIdRepository,
    private val exchangePairRepository: ExchangePairRepository,
) : Job {
    override fun execute(context: JobExecutionContext) {
        val blockId = blockIdRepository.findTopByWorkchainOrderBySeqno(-1).orElse(null) ?: return
        logger.debug { "started for blockId ${blockId.workchain}:${blockId.shard}:${blockId.seqno}" }

        for (exchangePair in exchangePairRepository.findAll()) {
            val reserveJobKey =
                JobKey("ReserveJob_${exchangePair.address.toSafeString()}_${blockId.id}", "ReserveJob")

            if (!scheduler.checkExists(reserveJobKey))
                scheduler.scheduleJob(
                    JobBuilder.newJob(ReserveJob::class.java)
                        .withIdentity(reserveJobKey)
                        .usingJobData(
                            JobDataMap(
                                mapOf(
                                    "address" to exchangePair.address,
                                    "blockId" to blockId,
                                )
                            )
                        )
                        .build(),
                    TriggerBuilder.newTrigger()
                        .withIdentity(
                            "ReserveTrigger_${exchangePair.address.toSafeString()}_${blockId.id}",
                            "ReserveTrigger"
                        )
                        .startNow()
                        .build()
                )
        }
    }

    companion object : KLogging()
}
