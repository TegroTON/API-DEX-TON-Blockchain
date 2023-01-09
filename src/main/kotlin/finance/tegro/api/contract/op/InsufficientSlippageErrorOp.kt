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

data class InsufficientSlippageErrorOp(
    override val queryId: ULong,
    override val destination: MsgAddress,
    override val payload: Maybe<Cell>,
) : ErrorOp {
    companion object : TlbCodec<InsufficientSlippageErrorOp> by InsufficientSlippageErrorOpConstructor {
        @JvmStatic
        fun tlbCodec(): TlbConstructor<InsufficientSlippageErrorOp> = InsufficientSlippageErrorOpConstructor
    }
}

private object InsufficientSlippageErrorOpConstructor : TlbConstructor<InsufficientSlippageErrorOp>(
    schema = "insufficient_slippage_error#c6d0084d query_id:uint64 destination:MsgAddress payload:(Maybe ^Cell) = InternalMsgBody;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: InsufficientSlippageErrorOp) {
        cellBuilder.apply {
            storeUInt64(value.queryId)
            storeTlb(MsgAddress, value.destination)
            storeTlb(Maybe.tlbCodec(AnyTlbConstructor), value.payload)
        }
    }

    override fun loadTlb(cellSlice: CellSlice): InsufficientSlippageErrorOp = cellSlice.run {
        InsufficientSlippageErrorOp(
            queryId = loadUInt64(),
            destination = loadTlb(MsgAddress),
            payload = loadTlb(Maybe.tlbCodec(AnyTlbConstructor)),
        )
    }
}
