package finance.tegro.observer.job

import finance.tegro.core.entity.BlockId
import finance.tegro.core.entity.ExchangePair
import finance.tegro.core.repository.ExchangePairRepository
import finance.tegro.core.toSafeString
import mu.KLogging
import org.quartz.*
import org.springframework.stereotype.Component
import org.ton.block.MsgAddress
import java.time.Instant

@Component
class ExchangePairJob(
    private val scheduler: Scheduler,

    private val exchangePairRepository: ExchangePairRepository,
) : Job {
    override fun execute(context: JobExecutionContext) {
        val jobData = context.mergedJobDataMap
        val address = jobData["address"] as MsgAddress
        val blockId = jobData["blockId"] as BlockId

        logger.debug { "started for address ${address.toSafeString()} and blockId ${blockId.workchain}:${blockId.shard}:${blockId.seqno}" }

        exchangePairRepository.findByAddress(address).orElse(null)
            ?: exchangePairRepository.save(
                ExchangePair(
                    address,
                    Instant.now(),
                )
            ).also { logger.debug { "ExchangePair ${it.address.toSafeString()} was created" } }


        val exchangePairAdminJobKey =
            JobKey("ExchangePairAdminJob_${address.toSafeString()}_${blockId.id}", "ExchangePairAdminJob")

        if (!scheduler.checkExists(exchangePairAdminJobKey))
            scheduler.scheduleJob(
                JobBuilder.newJob(ExchangePairAdminJob::class.java)
                    .withIdentity(exchangePairAdminJobKey)
                    .usingJobData(
                        JobDataMap(
                            mapOf(
                                "address" to address,
                                "blockId" to blockId
                            )
                        )
                    )
                    .build(),
                TriggerBuilder.newTrigger()
                    .withIdentity(
                        "ExchangePairAdminTrigger_${address.toSafeString()}_${blockId.id}",
                        "ExchangePairAdminTrigger"
                    )
                    .startNow()
                    .build()
            )

        val exchangePairTokenJobKey =
            JobKey("ExchangePairTokenJob_${address.toSafeString()}_${blockId.id}", "ExchangePairTokenJob")

        if (!scheduler.checkExists(exchangePairTokenJobKey))
            scheduler.scheduleJob(
                JobBuilder.newJob(ExchangePairTokenJob::class.java)
                    .withIdentity(exchangePairTokenJobKey)
                    .usingJobData(
                        JobDataMap(
                            mapOf(
                                "address" to address,
                                "blockId" to blockId
                            )
                        )
                    )
                    .build(),
                TriggerBuilder.newTrigger()
                    .withIdentity(
                        "ExchangePairTokenTrigger_${address.toSafeString()}_${blockId.id}",
                        "ExchangePairTokenTrigger"
                    )
                    .startNow()
                    .build()
            )

        val reserveJobKey =
            JobKey("ReserveJob_${address.toSafeString()}_${blockId.id}", "ReserveJob")

        if (!scheduler.checkExists(reserveJobKey))
            scheduler.scheduleJob(
                JobBuilder.newJob(ReserveJob::class.java)
                    .withIdentity(reserveJobKey)
                    .usingJobData(
                        JobDataMap(
                            mapOf(
                                "address" to address,
                                "blockId" to blockId
                            )
                        )
                    )
                    .build(),
                TriggerBuilder.newTrigger()
                    .withIdentity(
                        "ReserveTrigger_${address.toSafeString()}_${blockId.id}",
                        "ReserveTrigger"
                    )
                    .startNow()
                    .build()
            )
    }

    companion object : KLogging()
}
