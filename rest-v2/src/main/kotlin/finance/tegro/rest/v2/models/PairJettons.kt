@file:UseSerializers(RawAccountIdSerializer::class)

package finance.tegro.rest.v2.models

import finance.tegro.rest.v2.utils.RawAccountIdSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ton.lite.api.liteserver.LiteServerAccountId

@Serializable
data class PairJettons(
    val address: LiteServerAccountId,
    val base: LiteServerAccountId?,
    val quote: LiteServerAccountId?
) {
    override fun toString(): String = Json.encodeToString(this)
}
