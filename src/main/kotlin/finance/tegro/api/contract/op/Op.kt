package finance.tegro.api.contract.op

import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbCombinator
import org.ton.tlb.TlbConstructor

sealed interface Op {
    companion object : TlbCodec<Op> by OpCombinator
}

private object OpCombinator : TlbCombinator<Op>() {
    override val constructors: List<TlbConstructor<out Op>> =
        listOf(
            TransferOp.tlbCodec(), TransferNotificationOp.tlbCodec(), ExcessesOp.tlbCodec(),
            BurnOp.tlbCodec(), BurnNotificationOp.tlbCodec(), SwapTonOp.tlbCodec(),
            SuccessfulSwapOp.tlbCodec(),
        )

    override fun getConstructor(value: Op): TlbConstructor<out Op> = when (value) {
        is TransferOp -> TransferOp.tlbCodec()
        is TransferNotificationOp -> TransferNotificationOp.tlbCodec()
        is ExcessesOp -> ExcessesOp.tlbCodec()
        is BurnOp -> BurnOp.tlbCodec()
        is BurnNotificationOp -> BurnNotificationOp.tlbCodec()
        is SwapTonOp -> SwapTonOp.tlbCodec()
        is SuccessfulSwapOp -> SuccessfulSwapOp.tlbCodec()
    }
}
