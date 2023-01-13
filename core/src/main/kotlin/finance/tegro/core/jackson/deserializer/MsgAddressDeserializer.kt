package finance.tegro.core.jackson.deserializer

import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import finance.tegro.core.toMsgAddress
import org.ton.block.MsgAddress

class MsgAddressDeserializer : StdDeserializer<MsgAddress>(MsgAddress::class.java) {
    override fun deserialize(
        p0: com.fasterxml.jackson.core.JsonParser,
        p1: com.fasterxml.jackson.databind.DeserializationContext?
    ): MsgAddress =
        p0.valueAsString.toMsgAddress()
}
