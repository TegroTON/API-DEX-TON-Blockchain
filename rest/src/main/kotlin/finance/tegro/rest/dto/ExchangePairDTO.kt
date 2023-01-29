package finance.tegro.rest.dto

import com.fasterxml.jackson.annotation.JsonProperty
import org.ton.block.MsgAddress
import java.time.Instant

data class ExchangePairDTO(
    @JsonProperty("address")
    val address: MsgAddress,
    @JsonProperty("timestamp")
    val timestamp: Instant,

    @JsonProperty("liquidity")
    val liquidity: TokenDTO?,
    @JsonProperty("base")
    val base: TokenDTO?,
    @JsonProperty("quote")
    val quote: TokenDTO?,
    @JsonProperty("token_timestamp")
    val tokenTimestamp: Instant?,
) {
    fun inverse() = copy(
        base = quote,
        quote = base,
    )
}
