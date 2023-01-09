package finance.tegro.api.dto

import org.ton.block.MsgAddress
import java.math.BigInteger
import java.time.Instant

data class TotalLiquidityDTO(
    val exchangePair: MsgAddress,
    val liquidity: BigInteger,
    val timestamp: Instant,
)
