package finance.tegro.api.contract.op

import org.ton.block.Maybe
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbConstructor
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb

data class SuccessfulSwapOp(
    val queryId: ULong,
    val payload: Maybe<Cell>,
) : Op {
    companion object : TlbCodec<SuccessfulSwapOp> by SuccessfulSwapOpConstructor {
        @JvmStatic
        fun tlbCodec(): TlbConstructor<SuccessfulSwapOp> = SuccessfulSwapOpConstructor
    }
}

private object SuccessfulSwapOpConstructor : TlbConstructor<SuccessfulSwapOp>(
    schema = "successful_swap#de6e0675 query_id:uint64 payload:(Maybe ^Cell) = InternalMsgBody;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: SuccessfulSwapOp) {
        cellBuilder.apply {
            storeUInt64(value.queryId)
            storeTlb(Maybe.tlbCodec(AnyTlbConstructor), value.payload)
        }
    }

    override fun loadTlb(cellSlice: CellSlice): SuccessfulSwapOp = cellSlice.run {
        SuccessfulSwapOp(
            queryId = loadUInt64(),
            payload = loadTlb(Maybe.tlbCodec(AnyTlbConstructor)),
        )
    }
}
