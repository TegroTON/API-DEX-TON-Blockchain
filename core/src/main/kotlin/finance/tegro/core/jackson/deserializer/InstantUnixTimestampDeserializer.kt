package finance.tegro.core.jackson.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.time.Instant

class InstantUnixTimestampDeserializer : StdDeserializer<Instant>(Instant::class.java) {
    override fun deserialize(p0: JsonParser, p1: DeserializationContext?): Instant {
        return Instant.ofEpochSecond(p0.valueAsLong)
    }
}
