package finance.tegro.core.contract.op

import org.ton.block.Maybe
import org.ton.block.MsgAddress
import org.ton.block.VarUInteger
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbConstructor
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb

data class BurnOp(
    val queryId: ULong,
    val amount: VarUInteger,
    val responseDestination: MsgAddress,
    val customPayload: Maybe<Cell>,
) : Op {
    companion object : TlbCodec<BurnOp> by BurnOpConstructor {
        @JvmStatic
        fun tlbCodec(): TlbConstructor<BurnOp> = BurnOpConstructor
    }
}

private object BurnOpConstructor : TlbConstructor<BurnOp>(
    schema = "burn#595f07bc query_id:uint64 amount:(VarUInteger 16)\n" +
            "              response_destination:MsgAddress custom_payload:(Maybe ^Cell)\n" +
            "              = InternalMsgBody;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: BurnOp) {
        cellBuilder.apply {
            storeUInt64(value.queryId)
            storeTlb(VarUInteger.tlbCodec(16), value.amount)
            storeTlb(MsgAddress, value.responseDestination)
            storeTlb(Maybe.tlbCodec(AnyTlbConstructor), value.customPayload)
        }
    }

    override fun loadTlb(cellSlice: CellSlice): BurnOp = cellSlice.run {
        BurnOp(
            queryId = loadUInt64(),
            amount = loadTlb(VarUInteger.tlbCodec(16)),
            responseDestination = loadTlb(MsgAddress),
            customPayload = loadTlb(Maybe.tlbCodec(AnyTlbConstructor))
        )
    }
}
