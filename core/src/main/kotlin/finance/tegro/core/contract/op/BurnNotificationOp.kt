package finance.tegro.core.contract.op

import org.ton.block.MsgAddress
import org.ton.block.VarUInteger
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb

data class BurnNotificationOp(
    val queryId: ULong,
    val amount: VarUInteger,
    val sender: MsgAddress,
    val responseDestination: MsgAddress,
) : Op {
    companion object : TlbCodec<BurnNotificationOp> by BurnNotificationOpConstructor {
        @JvmStatic
        fun tlbCodec(): TlbConstructor<BurnNotificationOp> = BurnNotificationOpConstructor
    }
}

private object BurnNotificationOpConstructor : TlbConstructor<BurnNotificationOp>(
    schema = "burn_notification#7bdd97de query_id:uint64 amount:(VarUInteger 16)\n" +
            "                              sender:MsgAddress response_destination:MsgAddress\n" +
            "                              = InternalMsgBody;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: BurnNotificationOp) {
        cellBuilder.apply {
            storeUInt64(value.queryId)
            storeTlb(VarUInteger.tlbCodec(16), value.amount)
            storeTlb(MsgAddress, value.sender)
            storeTlb(MsgAddress, value.responseDestination)
        }
    }

    override fun loadTlb(cellSlice: CellSlice): BurnNotificationOp = cellSlice.run {
        BurnNotificationOp(
            queryId = loadUInt64(),
            amount = loadTlb(VarUInteger.tlbCodec(16)),
            sender = loadTlb(MsgAddress),
            responseDestination = loadTlb(MsgAddress)
        )
    }
}
