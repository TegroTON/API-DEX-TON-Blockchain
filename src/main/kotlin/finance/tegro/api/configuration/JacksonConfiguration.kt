package finance.tegro.api.configuration

import com.fasterxml.jackson.databind.module.SimpleModule
import finance.tegro.api.converter.jackson.BigIntegerDeserializer
import finance.tegro.api.converter.jackson.BigIntegerSerializer
import finance.tegro.api.converter.jackson.MsgAddressDeserializer
import finance.tegro.api.converter.jackson.MsgAddressSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.ton.block.MsgAddress
import java.math.BigInteger

@Configuration
class JacksonConfiguration {
    @Bean
    fun msgAddressModule() = SimpleModule().apply {
        addDeserializer(MsgAddress::class.java, MsgAddressDeserializer())
        addSerializer(MsgAddress::class.java, MsgAddressSerializer())
    }

    @Bean
    fun bigIntegerModule() = SimpleModule().apply {
        addSerializer(BigInteger::class.java, BigIntegerSerializer())
        addDeserializer(BigInteger::class.java, BigIntegerDeserializer())
    }
}
