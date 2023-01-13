package finance.tegro.rest.dto

import org.ton.block.MsgAddress
import java.math.BigInteger
import java.time.Instant

data class LiquidityDTO(
    val address: MsgAddress,
    val owner: MsgAddress,
    val exchangePair: MsgAddress,
    val liquidityToken: TokenDTO?,
    val balance: BigInteger,
    val timestamp: Instant,
)
