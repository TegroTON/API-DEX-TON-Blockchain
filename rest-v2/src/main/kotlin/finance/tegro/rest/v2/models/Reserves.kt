@file:UseSerializers(EpochSecondsSerializer::class, StringBigIntegerSerializer::class, RawAccountIdSerializer::class)

package finance.tegro.rest.v2.models

import finance.tegro.rest.v2.utils.EpochSecondsSerializer
import finance.tegro.rest.v2.utils.RawAccountIdSerializer
import finance.tegro.rest.v2.utils.StringBigIntegerSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.lite.api.liteserver.LiteServerAccountId
import java.math.BigInteger

@Serializable
data class Reserves(
    val address: LiteServerAccountId,
    val base: BigInteger,
    val quote: BigInteger
)
