package finance.tegro.api.configuration

import mu.KLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import java.time.Instant
import java.util.*

@Configuration
class ScheduleConfiguration(
    private val updateExchangePairJob: Job,
    private val updateReserveJob: Job,
    private val updateTokenJob: Job,

    private val jobLauncher: JobLauncher,
) {
    @Scheduled(cron = "\${schedule.exchange_pair:0 0 0 * * *}")
    fun scheduledUpdateExchangePairJob() {
        logger.info("Starting scheduled update exchange pair job")

        jobLauncher.run(
            updateExchangePairJob,
            JobParametersBuilder()
                .addDate("scheduled", Date.from(Instant.now()))
                .toJobParameters()
        )
    }

    @Scheduled(cron = "\${schedule.reserve:0 */10 * * * *}")
    fun scheduledUpdateReserveJob() {
        logger.info("Starting scheduled update reserve job")

        jobLauncher.run(
            updateReserveJob,
            JobParametersBuilder()
                .addDate("scheduled", Date.from(Instant.now()))
                .toJobParameters()
        )
    }

    @Scheduled(cron = "\${schedule.token:0 0 */4 * * *}")
    fun scheduledUpdateTokenJob() {
        logger.info("Starting scheduled update token job")

        jobLauncher.run(
            updateTokenJob,
            JobParametersBuilder()
                .addDate("scheduled", Date.from(Instant.now()))
                .toJobParameters()
        )
    }

    companion object : KLogging()
}
