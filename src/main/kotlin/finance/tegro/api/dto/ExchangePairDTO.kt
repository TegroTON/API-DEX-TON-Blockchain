package finance.tegro.api.dto

import org.ton.block.MsgAddress
import java.math.BigInteger
import java.time.Instant
import java.util.*

data class ExchangePairDTO(
    val id: UUID,
    val address: MsgAddress,
    val approved: Boolean,
    val timestamp: Instant,

    val admin: MsgAddress,
    val adminTimestamp: Instant,

    val tokenBase: MsgAddress,
    val tokenQuote: MsgAddress,
    val tokenTimestamp: Instant,

    val reserveBase: BigInteger,
    val reserveQuote: BigInteger,
    val reserveTimestamp: Instant,
) {
    fun inverse() = copy(
        tokenBase = tokenQuote,
        tokenQuote = tokenBase,
        reserveBase = reserveQuote,
        reserveQuote = reserveBase,
    )
}
