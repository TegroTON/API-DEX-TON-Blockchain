package finance.tegro.api.configuration

import finance.tegro.api.TonLogger
import finance.tegro.api.properties.LiteClientProperties
import mu.KLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.ton.lite.client.LiteClient


@Configuration
class LiteClientConfiguration(
    private val liteClientProperties: LiteClientProperties,
) {
    @Bean
    fun liteClient() = LiteClient(
        liteClientProperties.toLiteServerDesc(),
        TonLogger(),
    )

    companion object : KLogging()
}
