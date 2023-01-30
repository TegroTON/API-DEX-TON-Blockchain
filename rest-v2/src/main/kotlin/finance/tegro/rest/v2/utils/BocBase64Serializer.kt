package finance.tegro.rest.v2.utils

import io.ktor.util.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ton.boc.BagOfCells
import org.ton.cell.Cell

object CellBase64Serializer : KSerializer<Cell> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Cell", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Cell {
        return BagOfCells(decoder.decodeString().decodeBase64Bytes()).first()
    }

    override fun serialize(encoder: Encoder, value: Cell) {
        encoder.encodeString(BagOfCells(value).toByteArray().encodeBase64())
    }
}
