package finance.tegro.core.jackson.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.math.BigDecimal

class BigDecimalStringSerializer : StdSerializer<BigDecimal>(BigDecimal::class.java) {
    override fun serialize(value: BigDecimal, gen: JsonGenerator, provider: SerializerProvider?) {
        gen.writeString(value.toString())
    }
}
