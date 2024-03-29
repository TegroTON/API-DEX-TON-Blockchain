package finance.tegro.core.configuration

import finance.tegro.core.toMsgAddress
import finance.tegro.core.toSafeString
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.ton.block.MsgAddress

@Configuration
class ConverterConfiguration {
    @Bean
    fun msgAddressToStringConverter() = object : Converter<MsgAddress, String> {
        override fun convert(source: MsgAddress): String? = source.toSafeString()
    }

    @Bean
    fun stringToMsgAddressConverter() = object : Converter<String, MsgAddress> {
        override fun convert(source: String): MsgAddress = source.toMsgAddress()
    }
}
