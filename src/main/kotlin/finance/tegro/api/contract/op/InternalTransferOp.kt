package finance.tegro.api.contract.op

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

data class InternalTransferOp(
    val queryId: ULong,
    val amount: VarUInteger,
    val from: MsgAddress,
    val responseAddress: MsgAddress,
    val forwardTonAmount: VarUInteger,
    val forwardPayload: Either<Cell, Cell>
) : Op {
    companion object : TlbCodec<InternalTransferOp> by InternalTransferOpConstructor {
        @JvmStatic
        fun tlbCodec(): TlbConstructor<InternalTransferOp> = InternalTransferOpConstructor
    }
}

private object InternalTransferOpConstructor : TlbConstructor<InternalTransferOp>(
    schema = "internal_transfer#178d4519 query_id:uint64 amount:(VarUInteger 16) from:MsgAddress\n" +
            "                     response_address:MsgAddress\n" +
            "                     forward_ton_amount:(VarUInteger 16)\n" +
            "                     forward_payload:(Either Cell ^Cell)\n" +
            "                     = InternalMsgBody;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: InternalTransferOp) {
        cellBuilder.apply {
            storeUInt64(value.queryId)
            storeTlb(VarUInteger.tlbCodec(16), value.amount)
            storeTlb(MsgAddress, value.from)
            storeTlb(MsgAddress, value.responseAddress)
            storeTlb(VarUInteger.tlbCodec(16), value.forwardTonAmount)
            storeTlb(Either.tlbCodec(AnyTlbConstructor, Cell.tlbCodec()), value.forwardPayload)
        }
    }

    override fun loadTlb(cellSlice: CellSlice): InternalTransferOp = cellSlice.run {
        InternalTransferOp(
            queryId = loadUInt64(),
            amount = loadTlb(VarUInteger.tlbCodec(16)),
            from = loadTlb(MsgAddress),
            responseAddress = loadTlb(MsgAddress),
            forwardTonAmount = loadTlb(VarUInteger.tlbCodec(16)),
            forwardPayload = loadTlb(Either.tlbCodec(AnyTlbConstructor, Cell.tlbCodec()))
        )
    }


}
