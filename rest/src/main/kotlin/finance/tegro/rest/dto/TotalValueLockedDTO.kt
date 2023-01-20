package finance.tegro.rest.dto

import com.fasterxml.jackson.annotation.JsonProperty
import org.ton.block.MsgAddress
import java.math.BigInteger
import java.time.Instant

data class TotalValueLockedDTO(
    @JsonProperty("token")
    val token: TokenDTO?,
    @JsonProperty("value_locked")
    val valueLocked: BigInteger,
    @JsonProperty("pairs")
    val pairs: List<MsgAddress>,

    @JsonProperty("timestamp")
    val timestamp: Instant = Instant.now(),
)
