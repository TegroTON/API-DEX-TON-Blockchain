package finance.tegro.api.contract.op

import finance.tegro.api.contract.SwapParams
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb

data class SwapJettonPayloadOp(
    val params: SwapParams,
) : PayloadOp {
    companion object : TlbCodec<SwapJettonPayloadOp> by SwapJettonPayloadOpConstructor {
        @JvmStatic
        fun tlbCodec(): TlbConstructor<SwapJettonPayloadOp> = SwapJettonPayloadOpConstructor
    }
}

private object SwapJettonPayloadOpConstructor : TlbConstructor<SwapJettonPayloadOp>(
    schema = "swap_jetton#01fb7a25 params:SwapParams = Payload;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: SwapJettonPayloadOp) {
        cellBuilder.apply {
            storeTlb(SwapParams, value.params)
        }
    }

    override fun loadTlb(cellSlice: CellSlice): SwapJettonPayloadOp = cellSlice.run {
        SwapJettonPayloadOp(
            params = loadTlb(SwapParams),
        )
    }
}
