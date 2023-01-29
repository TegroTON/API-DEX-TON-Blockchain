@file:UseSerializers(EpochSecondsSerializer::class, StringBigIntegerSerializer::class)

package finance.tegro.rest.v2.dto.v1

import finance.tegro.rest.v2.utils.EpochSecondsSerializer
import finance.tegro.rest.v2.utils.StringBigIntegerSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.BigInteger

@Serializable
data class ExchangePairDTOv1(
    val address: String,
    val timestamp: Instant,

    val liquidity: TokenDTOv1?,
    val base: TokenDTOv1?,
    val quote: TokenDTOv1?,
    @SerialName("token_timestamp")
    val tokenTimestamp: Instant?,
    val reserve: ReserveDTOv1? = null
) {
    override fun toString(): String = Json.encodeToString(this)
}

@Serializable
data class TokenDTOv1(
    val address: String?,
    val timestamp: Instant,
    @SerialName("total_supply")
    val totalSupply: BigInteger?,
    val mintable: Boolean?,
    val admin: String?,
    @SerialName("contract_timestamp")
    val contractTimestamp: Instant?,

    val name: String?,
    val description: String?,
    val symbol: String?,
    val decimals: Int?,
    @SerialName("metadata_timestamp")
    val metadataTimestamp: Instant?
) {
    override fun toString(): String = Json.encodeToString(this)
}

@Serializable
data class ReserveDTOv1(
    val address: String,
    val base: BigInteger,
    val quote: BigInteger,
    val timestamp: Instant
) {
    override fun toString(): String = Json.encodeToString(this)
}
