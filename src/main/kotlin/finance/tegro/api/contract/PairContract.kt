package finance.tegro.api.contract

import mu.KLogging
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.bigint.BigInt
import org.ton.block.AddrStd
import org.ton.block.MsgAddress
import org.ton.block.VmStackValue
import org.ton.lite.api.liteserver.LiteServerAccountId
import org.ton.lite.client.LiteClient
import org.ton.tlb.loadTlb

interface PairContract {
    companion object : KLogging() {
        @JvmStatic
        suspend fun getPairTokens(
            address: AddrStd,
            liteClient: LiteClient,
            referenceBlock: TonNodeBlockIdExt?
        ): Pair<MsgAddress, MsgAddress> =
            liteClient.runSmcMethod(
                LiteServerAccountId(address),
                referenceBlock ?: liteClient.getLastBlockId(),
                "get::pair_tokens"
            ).toMutableVmStack().let {
                val baseSlice = it.popSlice()
                val quoteSlice = it.popSlice()
                require(it.isEmpty()) { "Stack is not empty" }

                val base = baseSlice.loadTlb(MsgAddress)
                val quote = quoteSlice.loadTlb(MsgAddress)

//                baseSlice.endParse()  // TODO
//                quoteSlice.endParse() // TODO

                base to quote
            }

        @JvmStatic
        suspend fun getLiquidityPoolShare(
            address: AddrStd,
            amount: BigInt,
            liteClient: LiteClient,
            referenceBlock: TonNodeBlockIdExt?
        ): Pair<BigInt, BigInt> =
            liteClient.runSmcMethod(
                LiteServerAccountId(address),
                referenceBlock ?: liteClient.getLastBlockId(),
                "get::lp_share",
                VmStackValue.of(amount)
            ).toMutableVmStack().let {
                val baseShare = it.popNumber().toBigInt()
                val quoteShare = it.popNumber().toBigInt()

                require(it.isEmpty()) { "Stack is not empty" }

                baseShare to quoteShare
            }

        @JvmStatic
        suspend fun getReserves(
            address: AddrStd,
            liteClient: LiteClient,
            referenceBlock: TonNodeBlockIdExt?
        ): Pair<BigInt, BigInt> =
            liteClient.runSmcMethod(
                LiteServerAccountId(address),
                referenceBlock ?: liteClient.getLastBlockId(),
                "get::reserves"
            ).toMutableVmStack().let {
                val baseReserve = it.popNumber().toBigInt()
                val quoteReserve = it.popNumber().toBigInt()

                require(it.isEmpty()) { "Stack is not empty" }

                baseReserve to quoteReserve
            }

        @JvmStatic
        suspend fun getAdminAddress(
            address: AddrStd,
            liteClient: LiteClient,
            referenceBlock: TonNodeBlockIdExt?
        ): MsgAddress =
            liteClient.runSmcMethod(
                LiteServerAccountId(address),
                referenceBlock ?: liteClient.getLastBlockId(),
                "get::admin_address"
            ).toMutableVmStack().let {
                val adminAddressSlice = it.popSlice()
                require(it.isEmpty()) { "Stack is not empty" }

                val adminAddress = adminAddressSlice.loadTlb(MsgAddress)
                adminAddressSlice.endParse()

                adminAddress
            }
    }
}
