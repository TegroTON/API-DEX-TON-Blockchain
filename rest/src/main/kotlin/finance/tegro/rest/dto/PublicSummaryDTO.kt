package finance.tegro.rest.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class PublicSummaryDTO(
    @JsonProperty("trading_pairs")
    val tradingPairs: String,
    @JsonProperty("last_price")
    val lastPrice: BigDecimal,
    @JsonProperty("base_volume")
    val baseVolume: BigDecimal,
    @JsonProperty("quote_volume")
    val quoteVolume: BigDecimal,
    @JsonProperty("price_change_percent_24h")
    val priceChangePercent24h: BigDecimal,
    @JsonProperty("highest_price_24h")
    val highestPrice24h: BigDecimal,
    @JsonProperty("lowest_price_24h")
    val lowestPrice24h: BigDecimal,
)
