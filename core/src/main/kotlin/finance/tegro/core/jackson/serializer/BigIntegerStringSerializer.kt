package finance.tegro.core.jackson.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.math.BigInteger

class BigIntegerStringSerializer : StdSerializer<BigInteger>(BigInteger::class.java) {
    override fun serialize(value: BigInteger, gen: JsonGenerator, provider: SerializerProvider?) {
        gen.writeString(value.toString())
    }
}
