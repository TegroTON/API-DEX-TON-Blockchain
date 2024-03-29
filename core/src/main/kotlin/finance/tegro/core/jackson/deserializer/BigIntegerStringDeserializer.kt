package finance.tegro.core.jackson.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.math.BigInteger

class BigIntegerStringDeserializer : StdDeserializer<BigInteger>(BigInteger::class.java) {
    override fun deserialize(p0: JsonParser, p1: DeserializationContext?): BigInteger {
        return BigInteger(p0.valueAsString)
    }
}
