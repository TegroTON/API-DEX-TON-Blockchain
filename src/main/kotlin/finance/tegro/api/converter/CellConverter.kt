package finance.tegro.api.converter

import org.ton.boc.BagOfCells
import org.ton.cell.Cell
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter(autoApply = true)
class CellConverter : AttributeConverter<Cell, ByteArray> {
    override fun convertToDatabaseColumn(cell: Cell): ByteArray =
        BagOfCells(cell).toByteArray()

    override fun convertToEntityAttribute(aByte: ByteArray): Cell =
        BagOfCells(aByte).roots.first()
}
