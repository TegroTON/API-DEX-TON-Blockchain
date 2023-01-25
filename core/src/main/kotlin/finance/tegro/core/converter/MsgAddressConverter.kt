package finance.tegro.core.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.ton.block.MsgAddress
import org.ton.boc.BagOfCells
import org.ton.cell.CellBuilder
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb

@Converter(autoApply = true)
class MsgAddressConverter : AttributeConverter<MsgAddress, ByteArray> {
    override fun convertToDatabaseColumn(msgAddress: MsgAddress): ByteArray =
        BagOfCells(CellBuilder.createCell { storeTlb(MsgAddress, msgAddress) }).toByteArray()

    override fun convertToEntityAttribute(aByte: ByteArray): MsgAddress =
        BagOfCells(aByte).roots.first().parse { loadTlb(MsgAddress) }
}
