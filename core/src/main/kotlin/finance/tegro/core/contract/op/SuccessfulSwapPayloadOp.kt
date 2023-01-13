package finance.tegro.core.contract.op

import org.ton.block.Maybe
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbConstructor
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb

data class SuccessfulSwapPayloadOp(
    val payload: Maybe<Cell>
) : PayloadOp {
    companion object : TlbCodec<SuccessfulSwapPayloadOp> by SuccessfulSwapPayloadOpConstructor {
        @JvmStatic
        fun tlbCodec(): TlbConstructor<SuccessfulSwapPayloadOp> = SuccessfulSwapPayloadOpConstructor
    }
}

private object SuccessfulSwapPayloadOpConstructor : TlbConstructor<SuccessfulSwapPayloadOp>(
    schema = "successful_swap_payload#de6e0675 payload:(Maybe ^Cell) = Payload;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: SuccessfulSwapPayloadOp) {
        cellBuilder.apply {
            storeTlb(Maybe.tlbCodec(AnyTlbConstructor), value.payload)
        }
    }

    override fun loadTlb(cellSlice: CellSlice): SuccessfulSwapPayloadOp = cellSlice.run {
        SuccessfulSwapPayloadOp(
            payload = loadTlb(Maybe.tlbCodec(AnyTlbConstructor)),
        )
    }
}
