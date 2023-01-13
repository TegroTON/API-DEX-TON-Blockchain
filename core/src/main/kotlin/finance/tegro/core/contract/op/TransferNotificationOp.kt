package finance.tegro.core.contract.op

import org.ton.block.Either
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

data class TransferNotificationOp(
    val queryId: ULong,
    val amount: VarUInteger,
    val sender: MsgAddress,
    val forwardPayload: Either<Cell, Cell>,
) : Op {
    companion object : TlbCodec<TransferNotificationOp> by TransferNotificationOpConstructor {
        @JvmStatic
        fun tlbCodec(): TlbConstructor<TransferNotificationOp> = TransferNotificationOpConstructor
    }
}

private object TransferNotificationOpConstructor : TlbConstructor<TransferNotificationOp>(
    schema = "transfer_notification#7362d09c query_id:uint64 amount:(VarUInteger 16)\n" +
            "                              sender:MsgAddress forward_payload:(Either Cell ^Cell)\n" +
            "                              = InternalMsgBody;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: TransferNotificationOp) {
        cellBuilder.apply {
            storeUInt64(value.queryId)
            storeTlb(VarUInteger.tlbCodec(16), value.amount)
            storeTlb(MsgAddress, value.sender)
            storeTlb(Either.tlbCodec(AnyTlbConstructor, Cell.tlbCodec()), value.forwardPayload)
        }
    }

    override fun loadTlb(cellSlice: CellSlice): TransferNotificationOp = cellSlice.run {
        TransferNotificationOp(
            queryId = loadUInt64(),
            amount = loadTlb(VarUInteger.tlbCodec(16)),
            sender = loadTlb(MsgAddress),
            forwardPayload = loadTlb(Either.tlbCodec(AnyTlbConstructor, Cell.tlbCodec()))
        )
    }
}
