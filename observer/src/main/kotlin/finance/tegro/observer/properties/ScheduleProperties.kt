package finance.tegro.observer.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(prefix = "observer.schedule")
@ConstructorBinding
data class ScheduleProperties(
    @DefaultValue("0 0 0/4 * * ?")
    val exchangePairCron: String,

    @DefaultValue("0 0 0/1 * * ?")
    val tokenCron: String,

    @DefaultValue("0 0/10 * * * ?")
    val reserveCron: String,
)
