package finance.tegro.observer.job

import finance.tegro.core.repository.BlockIdRepository
import finance.tegro.core.repository.ExchangePairRepository
import finance.tegro.core.toSafeString
import mu.KLogging
import org.quartz.*
import org.springframework.stereotype.Component

@Component
class AllExchangePairJob(
    private val scheduler: Scheduler,

    private val blockIdRepository: BlockIdRepository,
    private val exchangePairRepository: ExchangePairRepository,
) : Job {
    override fun execute(context: JobExecutionContext) {
        val blockId = blockIdRepository.findFirstByWorkchainOrderByTimestampDesc(-1).orElse(null) ?: return
        logger.debug { "started for blockId ${blockId.workchain}:${blockId.shard}:${blockId.seqno}" }

        for (exchangePair in exchangePairRepository.findAll()) {
            val exchangePairJobKey =
                JobKey("ExchangePairJob_${exchangePair.address.toSafeString()}_${blockId.id}", "ExchangePairJob")

            if (!scheduler.checkExists(exchangePairJobKey))
                scheduler.scheduleJob(
                    JobBuilder.newJob(ExchangePairJob::class.java)
                        .withIdentity(exchangePairJobKey)
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
                            "ExchangePairTrigger_${exchangePair.address.toSafeString()}_${blockId.id}",
                            "ExchangePairTrigger"
                        )
                        .startNow()
                        .build()
                )
        }
    }

    companion object : KLogging()
}
