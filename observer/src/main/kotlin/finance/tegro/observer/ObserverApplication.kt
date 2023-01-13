package finance.tegro.observer

import mu.KLogging
import org.quartz.Scheduler
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@ConfigurationPropertiesScan("finance.tegro.observer.properties")
@EnableJpaRepositories("finance.tegro.core.repository")
@EntityScan("finance.tegro.core.entity")
@ComponentScan("finance.tegro.observer", "finance.tegro.core")
@SpringBootApplication
class ObserverApplication(
    private val scheduler: Scheduler,

    private val jobSchedule: JobSchedule,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        scheduler.start()
        logger.info("Joining thread, hit Ctrl+C to shutdown application")
        Thread.currentThread().join()
        scheduler.shutdown()
    }

    companion object : KLogging() {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<ObserverApplication>(*args)
        }
    }
}
