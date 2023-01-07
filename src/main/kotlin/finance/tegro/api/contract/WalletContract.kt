package finance.tegro.api.contract

import mu.KLogging
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.bigint.BigInt
import org.ton.block.AddrStd
import org.ton.block.MsgAddress
import org.ton.cell.Cell
import org.ton.lite.api.liteserver.LiteServerAccountId
import org.ton.lite.client.LiteClient
import org.ton.tlb.loadTlb

data class WalletContract(
    val balance: BigInt,
    val owner: MsgAddress,
    val jetton: MsgAddress,
    val walletCode: Cell
) {
    companion object : KLogging() {
        @JvmStatic
        suspend fun of(
            address: AddrStd,
            liteClient: LiteClient,
            referenceBlock: TonNodeBlockIdExt?
        ): WalletContract =
            liteClient.runSmcMethod(
                LiteServerAccountId(address),
                referenceBlock ?: liteClient.getLastBlockId(),
                "get_wallet_data"
            ).toMutableVmStack().let {
                val balance = it.popNumber().toBigInt()
                val ownerSlice = it.popSlice()
                val jettonSlice = it.popSlice()
                val walletCode = it.popCell()

                require(it.isEmpty()) { "Stack is not empty" }

                val owner = ownerSlice.loadTlb(MsgAddress)
                val jetton = jettonSlice.loadTlb(MsgAddress)
                ownerSlice.endParse()
                jettonSlice.endParse()

                WalletContract(balance, owner, jetton, walletCode)
            }
    }
}
