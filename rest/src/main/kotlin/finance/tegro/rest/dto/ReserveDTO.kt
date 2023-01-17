package finance.tegro.rest.dto

import com.fasterxml.jackson.annotation.JsonProperty
import org.ton.block.MsgAddress
import java.math.BigInteger
import java.time.Instant

data class ReserveDTO(
    @JsonProperty("address")
    val address: MsgAddress,
    @JsonProperty("base")
    val base: BigInteger,
    @JsonProperty("quote")
    val quote: BigInteger,
    @JsonProperty("timestamp")
    val timestamp: Instant,
) {
    fun inverse() = copy(
        base = quote,
        quote = base,
    )
}
