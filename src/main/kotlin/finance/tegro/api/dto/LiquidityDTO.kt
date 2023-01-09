package finance.tegro.api.dto

import org.ton.block.MsgAddress
import java.math.BigInteger
import java.time.Instant

data class LiquidityDTO(
    val owner: MsgAddress,
    val exchangePair: MsgAddress,
    val balance: BigInteger,
    val timestamp: Instant,
)
