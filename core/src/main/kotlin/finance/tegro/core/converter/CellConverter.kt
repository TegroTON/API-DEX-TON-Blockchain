package finance.tegro.core.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.ton.boc.BagOfCells
import org.ton.cell.Cell

@Converter(autoApply = true)
class CellConverter : AttributeConverter<Cell, ByteArray> {
    override fun convertToDatabaseColumn(cell: Cell): ByteArray =
        BagOfCells(cell).toByteArray()

    override fun convertToEntityAttribute(aByte: ByteArray): Cell =
        BagOfCells(aByte).roots.first()
}
