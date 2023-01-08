package finance.tegro.api.contract.op

import org.ton.block.Either
import org.ton.block.Maybe
import org.ton.block.MsgAddress
import org.ton.block.VarUInteger
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbConstructor
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.tlb.constructor.tlbCodec
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb

data class TransferOp(
    val queryId: ULong,
    val amount: VarUInteger,
    val destination: MsgAddress,
    val responseDestination: MsgAddress,
    val customPayload: Maybe<Cell>,
    val forwardTonAmount: VarUInteger,
    val forwardPayload: Either<Cell, Cell>,
) : Op {
    companion object : TlbCodec<TransferOp> by TransferOpConstructor {
        @JvmStatic
        fun tlbCodec(): TlbConstructor<TransferOp> = TransferOpConstructor
    }
}

private object TransferOpConstructor : TlbConstructor<TransferOp>(
    schema = "transfer#0f8a7ea5 query_id:uint64 amount:(VarUInteger 16) destination:MsgAddress\n" +
            "                 response_destination:MsgAddress custom_payload:(Maybe ^Cell)\n" +
            "                 forward_ton_amount:(VarUInteger 16) forward_payload:(Either Cell ^Cell)\n" +
            "                 = InternalMsgBody;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: TransferOp) {
        cellBuilder.apply {
            storeUInt64(value.queryId)
            storeTlb(VarUInteger.tlbCodec(16), value.amount)
            storeTlb(MsgAddress, value.destination)
            storeTlb(MsgAddress, value.responseDestination)
            storeTlb(Maybe.tlbCodec(AnyTlbConstructor), value.customPayload)
            storeTlb(VarUInteger.tlbCodec(16), value.forwardTonAmount)
            storeTlb(Either.tlbCodec(AnyTlbConstructor, Cell.tlbCodec()), value.forwardPayload)
        }
    }

    override fun loadTlb(cellSlice: CellSlice): TransferOp = cellSlice.run {
        TransferOp(
            queryId = loadUInt64(),
            amount = loadTlb(VarUInteger.tlbCodec(16)),
            destination = loadTlb(MsgAddress),
            responseDestination = loadTlb(MsgAddress),
            customPayload = loadTlb(Maybe.tlbCodec(AnyTlbConstructor)),
            forwardTonAmount = loadTlb(VarUInteger.tlbCodec(16)),
            forwardPayload = loadTlb(Either.tlbCodec(AnyTlbConstructor, Cell.tlbCodec()))
        )
    }
}
