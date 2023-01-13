package finance.tegro.core.contract.op

import org.ton.block.Maybe
import org.ton.block.MsgAddress
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbConstructor
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb

data class InsufficientLeftAmountErrorOp(
    override val queryId: ULong,
    override val destination: MsgAddress,
    override val payload: Maybe<Cell>,
) : ErrorOp {
    companion object : TlbCodec<InsufficientLeftAmountErrorOp> by InsufficientLeftAmountErrorOpConstructor {
        @JvmStatic
        fun tlbCodec(): TlbConstructor<InsufficientLeftAmountErrorOp> = InsufficientLeftAmountErrorOpConstructor
    }
}

private object InsufficientLeftAmountErrorOpConstructor : TlbConstructor<InsufficientLeftAmountErrorOp>(
    schema = "insufficient_left_amount_error#b135ad70 query_id:uint64 destination:MsgAddress payload:(Maybe ^Cell) = InternalMsgBody;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: InsufficientLeftAmountErrorOp) {
        cellBuilder.apply {
            storeUInt64(value.queryId)
            storeTlb(MsgAddress, value.destination)
            storeTlb(Maybe.tlbCodec(AnyTlbConstructor), value.payload)
        }
    }

    override fun loadTlb(cellSlice: CellSlice): InsufficientLeftAmountErrorOp = cellSlice.run {
        InsufficientLeftAmountErrorOp(
            queryId = loadUInt64(),
            destination = loadTlb(MsgAddress),
            payload = loadTlb(Maybe.tlbCodec(AnyTlbConstructor)),
        )
    }
}
