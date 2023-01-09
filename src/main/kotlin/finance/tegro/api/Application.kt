package finance.tegro.api

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@ConfigurationPropertiesScan("finance.tegro.api.properties")
@EnableJpaRepositories("finance.tegro.api.repository")
@EntityScan("finance.tegro.api.entity")
@EnableBatchProcessing
@EnableScheduling
@SpringBootApplication
class Application {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<Application>(*args)
        }
    }
}
