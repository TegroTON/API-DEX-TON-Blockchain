package finance.tegro.core.contract

import mu.KLogging
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.bigint.BigInt
import org.ton.block.*
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.lite.api.liteserver.LiteServerAccountId
import org.ton.lite.client.LiteClient
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb

data class JettonContract(
    val totalSupply: BigInt,
    val mintable: Boolean,
    val admin: MsgAddress,
    val content: Cell,
    val walletCode: Cell
) {
    companion object : KLogging() {
        @JvmStatic
        fun ofTon() = JettonContract( // TODO: Fetch actual info?
            totalSupply = BigInt(5_000_000_000_000_000_000),
            mintable = false,
            admin = AddrNone,
            content = Cell.of(), // TODO: Put default metadata into contract's content
            walletCode = Cell.of()
        )

        @JvmStatic
        suspend fun of(address: AddrStd, liteClient: LiteClient, referenceBlock: TonNodeBlockIdExt?): JettonContract =
            liteClient.runSmcMethod(
                LiteServerAccountId(address),
                referenceBlock ?: liteClient.getLastBlockId(),
                "get_jetton_data"
            ).toMutableVmStack().let {
                val totalSupply = it.popNumber().toBigInt()
                val mintable = it.popNumber().toLong() != 0L
                val adminSlice = it.popSlice()
                val content = it.popCell()
                val walletCode = it.popCell()

                require(it.isEmpty()) { "Stack is not empty" }

                val admin = adminSlice.loadTlb(MsgAddressInt)
                adminSlice.endParse()

                JettonContract(totalSupply, mintable, admin, content, walletCode)
            }

        @JvmStatic
        suspend fun getWalletAddress(
            address: AddrStd,
            owner: MsgAddress,
            liteClient: LiteClient,
            referenceBlock: TonNodeBlockIdExt?
        ): MsgAddress =
            liteClient.runSmcMethod(
                LiteServerAccountId(address),
                referenceBlock ?: liteClient.getLastBlockId(),
                "get_wallet_address",
                VmStackValue.of(CellBuilder.createCell { storeTlb(MsgAddress, owner) }.beginParse())
            ).toMutableVmStack().let {
                val walletAddressSlice = it.popSlice()
                require(it.isEmpty()) { "Stack is not empty" }

                val walletAddress = walletAddressSlice.loadTlb(MsgAddress)
                walletAddressSlice.endParse()

                walletAddress
            }
    }
}
