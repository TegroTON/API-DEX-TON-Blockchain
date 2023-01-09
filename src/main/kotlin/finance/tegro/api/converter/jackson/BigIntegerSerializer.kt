package finance.tegro.api.converter.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.math.BigInteger

class BigIntegerSerializer : StdSerializer<BigInteger>(BigInteger::class.java) {
    override fun serialize(value: BigInteger, gen: JsonGenerator, provider: SerializerProvider?) {
        gen.writeString(value.toString())
    }
}
