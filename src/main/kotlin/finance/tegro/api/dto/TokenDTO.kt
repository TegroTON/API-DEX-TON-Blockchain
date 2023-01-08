package finance.tegro.api.dto

import org.ton.block.MsgAddress
import java.math.BigInteger
import java.time.Instant

data class TokenDTO(
    val address: MsgAddress,
    val approved: Boolean,
    val timestamp: Instant,

    val totalSupply: BigInteger,
    val mintable: Boolean,
    val admin: MsgAddress,
    val contractTimestamp: Instant,

    val name: String?,
    val description: String?,
    val image: String?,
    val symbol: String?,
    val decimals: Int,
    val metadataTimestamp: Instant,
)
