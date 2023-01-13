package finance.tegro.rest.dto

import org.ton.block.MsgAddress
import java.time.Instant

data class ExchangePairDTO(
    val address: MsgAddress,
    val timestamp: Instant,

    val liquidity: TokenDTO?,
    val base: TokenDTO?,
    val quote: TokenDTO?,
    val tokenTimestamp: Instant?,

    val reserve: ReserveDTO?,
) {
    fun inverse() = copy(
        base = quote,
        quote = base,
        reserve = reserve?.inverse(),
    )
}
