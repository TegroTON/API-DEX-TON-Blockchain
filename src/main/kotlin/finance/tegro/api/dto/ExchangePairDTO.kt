package finance.tegro.api.dto

import org.ton.block.MsgAddress
import java.math.BigInteger
import java.time.Instant
import java.util.*

data class ExchangePairDTO(
    var id: UUID,
    var address: MsgAddress,
    var timestamp: Instant,

    var tokenBase: MsgAddress,
    var tokenQuote: MsgAddress,
    var tokenTimestamp: Instant,

    var reserveBase: BigInteger,
    var reserveQuote: BigInteger,
    var reserveTimestamp: Instant,
) {
    fun inverse() = copy(
        tokenBase = tokenQuote,
        tokenQuote = tokenBase,
        reserveBase = reserveQuote,
        reserveQuote = reserveBase,
    )
}
