package finance.tegro.api.converter.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import finance.tegro.api.toSafeString
import org.ton.block.MsgAddress

class MsgAddressSerializer : StdSerializer<MsgAddress>(MsgAddress::class.java) {
    override fun serialize(value: MsgAddress, gen: JsonGenerator, provider: SerializerProvider?) {
        value.toSafeString()?.let { gen.writeString(it) } ?: gen.writeNull()
    }
}
