package finance.tegro.core.jackson.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.math.BigDecimal

class BigDecimalStringDeserializer : StdDeserializer<BigDecimal>(BigDecimal::class.java) {
    override fun deserialize(p0: JsonParser, p1: DeserializationContext?): BigDecimal {
        return BigDecimal(p0.valueAsString)
    }
}
