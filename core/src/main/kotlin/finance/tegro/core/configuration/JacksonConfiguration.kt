package finance.tegro.core.configuration

import com.fasterxml.jackson.databind.module.SimpleModule
import finance.tegro.core.jackson.deserializer.BigIntegerStringDeserializer
import finance.tegro.core.jackson.deserializer.InstantUnixTimestampDeserializer
import finance.tegro.core.jackson.deserializer.MsgAddressDeserializer
import finance.tegro.core.jackson.serializer.BigIntegerStringSerializer
import finance.tegro.core.jackson.serializer.InstantUnixTimestampSerializer
import finance.tegro.core.jackson.serializer.MsgAddressSerializer
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
        addSerializer(BigInteger::class.java, BigIntegerStringSerializer())
        addDeserializer(BigInteger::class.java, BigIntegerStringDeserializer())
    }

    @Bean
    fun instantModule() = SimpleModule().apply {
        addSerializer(java.time.Instant::class.java, InstantUnixTimestampSerializer())
        addDeserializer(java.time.Instant::class.java, InstantUnixTimestampDeserializer())
    }
}
