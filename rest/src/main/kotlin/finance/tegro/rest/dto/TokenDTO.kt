package finance.tegro.rest.dto

import com.fasterxml.jackson.annotation.JsonProperty
import org.ton.block.MsgAddress
import java.math.BigInteger
import java.time.Instant

data class TokenDTO(
    @JsonProperty("address")
    val address: MsgAddress,
    @JsonProperty("timestamp")
    val timestamp: Instant,

    @JsonProperty("total_supply")
    val totalSupply: BigInteger?,
    @JsonProperty("mintable")
    val mintable: Boolean?,
    @JsonProperty("admin")
    val admin: MsgAddress?,
    @JsonProperty("contract_timestamp")
    val contractTimestamp: Instant?,

    @JsonProperty("name")
    val name: String?,
    @JsonProperty("description")
    val description: String?,
    @JsonProperty("symbol")
    val symbol: String?,
    @JsonProperty("decimals")
    val decimals: Int?,
    @JsonProperty("metadata_timestamp")
    val metadataTimestamp: Instant?,
)
