package finance.tegro.rest.dto

import com.fasterxml.jackson.annotation.JsonProperty
import org.ton.block.MsgAddress
import java.math.BigInteger
import java.time.Instant

data class LiquidityDTO(
    @JsonProperty("address")
    val address: MsgAddress,
    @JsonProperty("owner")
    val owner: MsgAddress,
    @JsonProperty("exchange_pair")
    val exchangePair: MsgAddress,
    @JsonProperty("liquidity_token")
    val liquidityToken: TokenDTO?,
    @JsonProperty("balance")
    val balance: BigInteger,
    @JsonProperty("timestamp")
    val timestamp: Instant,
)
