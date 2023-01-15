package finance.tegro.rest.dto

import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant

data class PublicTradeDTO(
    val tradeId: Long,
    val price: BigDecimal,
    val baseVolume: BigDecimal,
    val quoteVolume: BigDecimal,
    val timestamp: Instant,
    val type: String,
)
