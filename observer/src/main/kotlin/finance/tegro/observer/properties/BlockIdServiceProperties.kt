package finance.tegro.observer.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.time.Duration

@ConfigurationProperties(prefix = "observer.block.id")
@ConstructorBinding
data class BlockIdServiceProperties(
    @DefaultValue("PT1S")
    val pollRate: Duration,
)
