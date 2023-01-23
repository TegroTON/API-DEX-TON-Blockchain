package finance.tegro.rest

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.servers.Server
import mu.KLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@OpenAPIDefinition(
    servers = [Server(url = "/")]
)
@ConfigurationPropertiesScan("finance.tegro.rest.properties")
@EnableJpaRepositories("finance.tegro.core.repository")
@EntityScan("finance.tegro.core.entity")
@ComponentScan("finance.tegro.rest", "finance.tegro.core")
@SpringBootApplication
class RestApplication {
    companion object : KLogging() {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<RestApplication>(*args)
        }
    }
}
