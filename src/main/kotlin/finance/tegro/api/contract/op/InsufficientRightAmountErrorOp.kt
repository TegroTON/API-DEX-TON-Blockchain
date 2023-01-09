package finance.tegro.api.contract.op

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

data class InsufficientRightAmountErrorOp(
    override val queryId: ULong,
    override val destination: MsgAddress,
    override val payload: Maybe<Cell>,
) : ErrorOp {
    companion object : TlbCodec<InsufficientRightAmountErrorOp> by InsufficientRightAmountErrorOpConstructor {
        @JvmStatic
        fun tlbCodec(): TlbConstructor<InsufficientRightAmountErrorOp> = InsufficientRightAmountErrorOpConstructor
    }
}

private object InsufficientRightAmountErrorOpConstructor : TlbConstructor<InsufficientRightAmountErrorOp>(
    schema = "insufficient_right_amount_error#d1635ae2 query_id:uint64 destination:MsgAddress payload:(Maybe ^Cell) = InternalMsgBody;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: InsufficientRightAmountErrorOp) {
        cellBuilder.apply {
            storeUInt64(value.queryId)
            storeTlb(MsgAddress, value.destination)
            storeTlb(Maybe.tlbCodec(AnyTlbConstructor), value.payload)
        }
    }

    override fun loadTlb(cellSlice: CellSlice): InsufficientRightAmountErrorOp = cellSlice.run {
        InsufficientRightAmountErrorOp(
            queryId = loadUInt64(),
            destination = loadTlb(MsgAddress),
            payload = loadTlb(Maybe.tlbCodec(AnyTlbConstructor)),
        )
    }
}
