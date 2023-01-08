package finance.tegro.api.contract.op

import finance.tegro.api.contract.SwapParams
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb

data class SwapTonOp(
    val queryId: ULong,
    val params: SwapParams,
) : Op {
    companion object : TlbCodec<SwapTonOp> by SwapTonOpConstructor {
        @JvmStatic
        fun tlbCodec(): TlbConstructor<SwapTonOp> = SwapTonOpConstructor
    }
}

private object SwapTonOpConstructor : TlbConstructor<SwapTonOp>(
    schema = "swap_ton#600c00fd query_id:uint64 params:SwapParams = InternalMsgBody;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: SwapTonOp) {
        cellBuilder.apply {
            storeUInt64(value.queryId)
            storeTlb(SwapParams, value.params)
        }
    }

    override fun loadTlb(cellSlice: CellSlice): SwapTonOp = cellSlice.run {
        SwapTonOp(
            queryId = loadUInt64(),
            params = loadTlb(SwapParams),
        )
    }
}
