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

data class InsufficientLiquidityErrorOp(
    override val queryId: ULong,
    override val destination: MsgAddress,
    override val payload: Maybe<Cell>,
) : ErrorOp {
    companion object : TlbCodec<InsufficientLiquidityErrorOp> by InsufficientLiquidityErrorOpConstructor {
        @JvmStatic
        fun tlbCodec(): TlbConstructor<InsufficientLiquidityErrorOp> = InsufficientLiquidityErrorOpConstructor
    }
}

private object InsufficientLiquidityErrorOpConstructor : TlbConstructor<InsufficientLiquidityErrorOp>(
    schema = "insufficient_liquidity_error#cf7c0035 query_id:uint64 destination:MsgAddress payload:(Maybe ^Cell) = InternalMsgBody;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: InsufficientLiquidityErrorOp) {
        cellBuilder.apply {
            storeUInt64(value.queryId)
            storeTlb(MsgAddress, value.destination)
            storeTlb(Maybe.tlbCodec(AnyTlbConstructor), value.payload)
        }
    }

    override fun loadTlb(cellSlice: CellSlice): InsufficientLiquidityErrorOp = cellSlice.run {
        InsufficientLiquidityErrorOp(
            queryId = loadUInt64(),
            destination = loadTlb(MsgAddress),
            payload = loadTlb(Maybe.tlbCodec(AnyTlbConstructor)),
        )
    }
}
