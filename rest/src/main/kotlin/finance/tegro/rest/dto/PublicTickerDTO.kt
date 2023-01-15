package finance.tegro.rest.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class PublicTickerDTO(
    @JsonProperty("last_price")
    val lastPrice: BigDecimal,
    @JsonProperty("base_volume")
    val baseVolume: BigDecimal,
    @JsonProperty("quote_volume")
    val quoteVolume: BigDecimal,
)
