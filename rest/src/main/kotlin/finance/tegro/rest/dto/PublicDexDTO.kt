package finance.tegro.rest.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(name = "DEX", description = "Information about DEX exchange pair")
data class PublicDexDTO(
    @field:Schema(
        description = "Unique id of the exchange pair",
        example = "EQDTjWuJmwD5SJ8l8L0zoNy8mJP4aJ_k6b4Eg2vm88lCpAIC"
    )
    @JsonProperty("id")
    val id: String,

    @field:Schema(
        description = "Direct link to the exchange pair on DEX",
        example = "https://tegro.finance/swap?from=null&to=EQD0vdSA_NedR9uvbgN9EikRX-suesDxGeFg69XQMavfLqIw"
    )
    @JsonProperty("url")
    val url: String,

    @field:Schema(
        description = "Id of the base token, `null` is a valid ID of the native blockchain token (TON)",
        example = "null"
    )
    @JsonProperty("base_id")
    val baseId: String?,

    @field:Schema(
        description = "Name of the base token",
        example = "Toncoin"
    )
    @JsonProperty("base_name")
    val baseName: String,

    @field:Schema(
        description = "Symbol of the base token",
        example = "TON"
    )
    @JsonProperty("base_symbol")
    val baseSymbol: String,

    @field:Schema(
        description = "24h trading volume expressed in units of the base token",
        type = "string",
        example = "1.234567891"
    )
    @JsonProperty("base_volume")
    val baseVolume: BigDecimal,

    @field:Schema(
        description = "Id of the quote token, `null` is a valid ID of the native blockchain token (TON)",
        example = "EQAvDfWFG0oYX19jwNDNBBL1rKNT9XfaGP9HyTb5nb2Eml6y"
    )
    @JsonProperty("quote_id")
    val quoteId: String?,

    @field:Schema(
        description = "Name of the quote token",
        example = "Tegro"
    )
    @JsonProperty("quote_name")
    val quoteName: String,

    @field:Schema(
        description = "Symbol of the quote token",
        example = "TGR"
    )
    @JsonProperty("quote_symbol")
    val quoteSymbol: String,

    @field:Schema(
        description = "24h trading volume expressed in units of the quote token",
        type = "string",
        example = "1.234567891"
    )
    @JsonProperty("quote_volume")
    val quoteVolume: BigDecimal,

    @field:Schema(description = "last traded price", type = "string", example = "0.123456789")
    @JsonProperty("last_price")
    val lastPrice: BigDecimal?,
)
