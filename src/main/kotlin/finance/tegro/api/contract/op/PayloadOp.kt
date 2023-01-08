package finance.tegro.api.contract.op

import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbCombinator
import org.ton.tlb.TlbConstructor

sealed interface PayloadOp {
    companion object : TlbCodec<PayloadOp> by PayloadOpCombinator
}

private object PayloadOpCombinator : TlbCombinator<PayloadOp>() {
    override val constructors: List<TlbConstructor<out PayloadOp>> =
        listOf(
            AddLiquidityPayloadOp.tlbCodec(),
            SwapJettonPayloadOp.tlbCodec(),
            SuccessfulSwapPayloadOp.tlbCodec()
        )

    override fun getConstructor(value: PayloadOp): TlbConstructor<out PayloadOp> = when (value) {
        is AddLiquidityPayloadOp -> AddLiquidityPayloadOp.tlbCodec()
        is SwapJettonPayloadOp -> SwapJettonPayloadOp.tlbCodec()
        is SuccessfulSwapPayloadOp -> SuccessfulSwapPayloadOp.tlbCodec()
    }
}
