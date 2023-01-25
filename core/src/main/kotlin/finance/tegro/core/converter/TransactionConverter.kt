package finance.tegro.core.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.ton.block.Transaction
import org.ton.boc.BagOfCells
import org.ton.cell.CellBuilder
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb

@Converter(autoApply = true)
class TransactionConverter : AttributeConverter<Transaction, ByteArray> {
    override fun convertToDatabaseColumn(transaction: Transaction): ByteArray =
        BagOfCells(CellBuilder.createCell { storeTlb(Transaction, transaction) }).toByteArray()

    override fun convertToEntityAttribute(aByte: ByteArray): Transaction =
        BagOfCells(aByte).roots.first().parse { loadTlb(Transaction) }
}
