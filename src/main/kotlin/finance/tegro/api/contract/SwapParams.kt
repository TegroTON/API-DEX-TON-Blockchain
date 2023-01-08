package finance.tegro.api.contract

import org.ton.block.Coins
import org.ton.block.Maybe
import org.ton.block.MsgAddress
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbConstructor
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb

data class SwapParams(
    val extract: Boolean,
    val maxIn: Coins,
    val minOut: Coins,
    val destination: MsgAddress,
    val errorDestination: MsgAddress,
    val customPayload: Maybe<Cell>,
) {
    companion object : TlbCodec<SwapParams> by SwapParamsConstructor {
        @JvmStatic
        fun tlbCodec(): TlbConstructor<SwapParams> = SwapParamsConstructor
    }
}

private object SwapParamsConstructor : TlbConstructor<SwapParams>(
    schema = "swap_params#_ extract:Bool max_in:Coins min_out:Coins\n" +
            "              destination:MsgAddress error_destination:MsgAddress\n" +
            "              custom_payload:(Maybe ^Cell) = SwapParams;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: SwapParams) {
        cellBuilder.apply {
            storeBit(value.extract)
            storeTlb(Coins, value.maxIn)
            storeTlb(Coins, value.minOut)
            storeTlb(MsgAddress, value.destination)
            storeTlb(MsgAddress, value.errorDestination)
            storeTlb(Maybe.tlbCodec(AnyTlbConstructor), value.customPayload)
        }
    }

    override fun loadTlb(cellSlice: CellSlice): SwapParams = cellSlice.run {
        SwapParams(
            extract = loadBit(),
            maxIn = loadTlb(Coins),
            minOut = loadTlb(Coins),
            destination = loadTlb(MsgAddress),
            errorDestination = loadTlb(MsgAddress),
            customPayload = loadTlb(Maybe.tlbCodec(AnyTlbConstructor))
        )
    }
}
