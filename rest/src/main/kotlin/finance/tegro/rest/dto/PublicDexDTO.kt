package finance.tegro.rest.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class PublicDexDTO(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("url")
    val url: String,
    @JsonProperty("base_id")
    val baseId: String?,
    @JsonProperty("base_name")
    val baseName: String,
    @JsonProperty("base_symbol")
    val baseSymbol: String,
    @JsonProperty("base_volume")
    val baseVolume: BigDecimal?,
    @JsonProperty("quote_id")
    val quoteId: String?,
    @JsonProperty("quote_name")
    val quoteName: String,
    @JsonProperty("quote_symbol")
    val quoteSymbol: String,
    @JsonProperty("quote_volume")
    val quoteVolume: BigDecimal?,
    @JsonProperty("last_price")
    val lastPrice: BigDecimal?,
)
