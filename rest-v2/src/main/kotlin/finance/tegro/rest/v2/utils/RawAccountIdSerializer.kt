package finance.tegro.rest.v2.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ton.bitstring.Bits256
import org.ton.crypto.decodeHex
import org.ton.lite.api.liteserver.LiteServerAccountId

object RawAccountIdSerializer : KSerializer<LiteServerAccountId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LiteServerAccountId", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LiteServerAccountId {
        val (workchainId, id) = decoder.decodeString().split(":")
        return LiteServerAccountId(workchainId.toInt(), Bits256(id.decodeHex()))
    }

    override fun serialize(encoder: Encoder, value: LiteServerAccountId) {
        encoder.encodeString("${value.workchain}:${value.id.value}")
    }
}
