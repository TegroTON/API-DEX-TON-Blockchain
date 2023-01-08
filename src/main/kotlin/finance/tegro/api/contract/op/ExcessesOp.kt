package finance.tegro.api.contract.op

import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbConstructor

data class ExcessesOp(
    val queryId: ULong,
) : Op {
    companion object : TlbCodec<ExcessesOp> by ExcessesOpConstructor {
        @JvmStatic
        fun tlbCodec(): TlbConstructor<ExcessesOp> = ExcessesOpConstructor
    }
}

private object ExcessesOpConstructor : TlbConstructor<ExcessesOp>(
    schema = "excesses#d53276db query_id:uint64 = InternalMsgBody;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: ExcessesOp) {
        cellBuilder.apply {
            storeUInt64(value.queryId)
        }
    }

    override fun loadTlb(cellSlice: CellSlice): ExcessesOp = cellSlice.run {
        ExcessesOp(
            queryId = loadUInt64()
        )
    }
}
