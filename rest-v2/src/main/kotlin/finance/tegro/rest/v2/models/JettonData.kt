@file:UseSerializers(RawAccountIdSerializer::class, StringBigIntegerSerializer::class, CellBase64Serializer::class)

package finance.tegro.rest.v2.models

import finance.tegro.rest.v2.utils.CellBase64Serializer
import finance.tegro.rest.v2.utils.RawAccountIdSerializer
import finance.tegro.rest.v2.utils.StringBigIntegerSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ton.cell.Cell
import org.ton.lite.api.liteserver.LiteServerAccountId
import java.math.BigInteger

@Serializable
data class JettonData(
    @SerialName("total_supply")
    val totalSupply: BigInteger,
    val mintable: Boolean,
    val admin: LiteServerAccountId?,
    val content: JettonContent,
    @SerialName("wallet_code")
    val walletCode: Cell
) {
    override fun toString(): String = Json.encodeToString(this)
}
