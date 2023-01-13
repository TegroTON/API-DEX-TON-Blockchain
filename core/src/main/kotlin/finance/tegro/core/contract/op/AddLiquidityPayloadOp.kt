package finance.tegro.core.contract.op

import org.ton.block.Coins
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb

data class AddLiquidityPayloadOp(
    val minLeftAmount: Coins,
    val minRightAmount: Coins,
) : PayloadOp {
    companion object : TlbCodec<AddLiquidityPayloadOp> by AddLiquidityPayloadOpConstructor {
        @JvmStatic
        fun tlbCodec(): TlbConstructor<AddLiquidityPayloadOp> = AddLiquidityPayloadOpConstructor
    }
}

private object AddLiquidityPayloadOpConstructor : TlbConstructor<AddLiquidityPayloadOp>(
    schema = "add_liquidity#287e167a min_left_amount:Coins min_right_amount:Coins = Payload;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: AddLiquidityPayloadOp) {
        cellBuilder.apply {
            storeTlb(Coins, value.minLeftAmount)
            storeTlb(Coins, value.minRightAmount)
        }
    }

    override fun loadTlb(cellSlice: CellSlice): AddLiquidityPayloadOp = cellSlice.run {
        AddLiquidityPayloadOp(
            minLeftAmount = loadTlb(Coins),
            minRightAmount = loadTlb(Coins),
        )
    }
}
