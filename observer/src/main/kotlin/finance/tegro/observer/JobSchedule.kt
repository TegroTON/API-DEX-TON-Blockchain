package finance.tegro.observer

import finance.tegro.observer.job.*
import finance.tegro.observer.properties.ScheduleProperties
import mu.KLogging
import org.quartz.CronScheduleBuilder
import org.quartz.JobBuilder
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component

@Component
class JobSchedule(
    private val scheduler: Scheduler,
    private val scheduleProperties: ScheduleProperties,
) : InitializingBean {
    override fun afterPropertiesSet() {
        logger.info { "Scheduling on-startup jobs" }
        scheduler.scheduleJob(
            JobBuilder.newJob(AllExchangePairJob::class.java)
                .withIdentity("AllExchangePairJob_startup", "AllExchangePairJob")
                .build(),
            TriggerBuilder.newTrigger()
                .withIdentity("AllExchangePairTrigger_startup", "AllExchangePairTrigger")
                .startNow()
                .build()
        )
        scheduler.scheduleJob(
            JobBuilder.newJob(AllTokenJob::class.java)
                .withIdentity("AllTokenJob_startup", "AllTokenJob")
                .build(),
            TriggerBuilder.newTrigger()
                .withIdentity("AllTokenTrigger_startup", "AllTokenTrigger")
                .startNow()
                .build()
        )
        scheduler.scheduleJob(
            JobBuilder.newJob(AllReserveJob::class.java)
                .withIdentity("AllReserveJob_startup", "AllReserveJob")
                .build(),
            TriggerBuilder.newTrigger()
                .withIdentity("AllReserveTrigger_startup", "AllReserveTrigger")
                .startNow()
                .build()
        )

        logger.info { "Scheduling cron jobs" }
        scheduler.scheduleJob(
            JobBuilder.newJob(AllExchangePairJob::class.java)
                .withIdentity("AllExchangePairJob_cron", "AllExchangePairJob")
                .build(),
            TriggerBuilder.newTrigger()
                .withIdentity("AllExchangePairTrigger_cron", "AllExchangePairTrigger")
                .withSchedule(
                    CronScheduleBuilder.cronScheduleNonvalidatedExpression(scheduleProperties.exchangePairCron)
                )
                .startNow()
                .build()
        )
        scheduler.scheduleJob(
            JobBuilder.newJob(AllTokenJob::class.java)
                .withIdentity("AllTokenJob_cron", "AllTokenJob")
                .build(),
            TriggerBuilder.newTrigger()
                .withIdentity("AllTokenTrigger_cron", "AllTokenTrigger")
                .withSchedule(
                    CronScheduleBuilder.cronScheduleNonvalidatedExpression(scheduleProperties.tokenCron)
                )
                .startNow()
                .build()
        )
        scheduler.scheduleJob(
            JobBuilder.newJob(AllReserveJob::class.java)
                .withIdentity("AllReserveJob_cron", "AllReserveJob")
                .build(),
            TriggerBuilder.newTrigger()
                .withIdentity("AllReserveTrigger_cron", "AllReserveTrigger")
                .withSchedule(
                    CronScheduleBuilder.cronScheduleNonvalidatedExpression(scheduleProperties.reserveCron)
                )
                .startNow()
                .build()
        )
        scheduler.scheduleJob(
            JobBuilder.newJob(MasterchainBlockIdJob::class.java)
                .withIdentity("MasterchainBlockIdJob_cron", "MasterchainBlockIdJob")
                .build(),
            TriggerBuilder.newTrigger()
                .withIdentity("MasterchainBlockIdTrigger_cron", "MasterchainBlockIdTrigger")
                .withSchedule(
                    CronScheduleBuilder.cronScheduleNonvalidatedExpression(scheduleProperties.masterchainBlockCron)
                )
                .startNow()
                .build()
        )
        scheduler.scheduleJob(
            JobBuilder.newJob(CatchUpMissingBlockJob::class.java)
                .withIdentity("CatchUpMissingBlockJob_cron", "CatchUpMissingBlockJob")
                .build(),
            TriggerBuilder.newTrigger()
                .withIdentity("CatchUpMissingBlockTrigger_cron", "CatchUpMissingBlockTrigger")
                .withSchedule(
                    CronScheduleBuilder.cronScheduleNonvalidatedExpression(scheduleProperties.catchUpMissingBlockCron)
                )
                .startNow()
                .build()
        )
    }

    companion object : KLogging()
}
