package finance.tegro.api.processor

import finance.tegro.api.contract.JettonContract
import finance.tegro.api.contract.WalletContract
import finance.tegro.api.entity.Liquidity
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.block.AddrStd
import org.ton.block.MsgAddress
import org.ton.lite.client.LiteClient
import java.time.Instant

@Component
class FetchLiquidityProcessor(
    private val liteClient: LiteClient,
) : ItemProcessor<Pair<TonNodeBlockIdExt, Pair<MsgAddress, MsgAddress>>, Liquidity> {
    override fun process(item: Pair<TonNodeBlockIdExt, Pair<MsgAddress, MsgAddress>>): Liquidity {
        val (block, ownerAndExchangePair) = item
        val (owner, exchangePair) = ownerAndExchangePair

        val (walletAddress, wallet) = runBlocking {
            val walletAddress = JettonContract.getWalletAddress(
                checkNotNull(exchangePair as? AddrStd) { "Exchange pair address is not valid" },
                owner,
                liteClient,
                block
            )
            walletAddress to WalletContract.of(
                checkNotNull(walletAddress as? AddrStd) { "Liquidity wallet address is not valid" },
                liteClient,
                block
            )
        }

        return Liquidity(
            address = walletAddress,
            owner = wallet.owner,
            exchangePair = wallet.jetton,
            balance = wallet.balance,
            block = block,
            timestamp = Instant.now(),
        )
    }

    companion object : KLogging()
}
