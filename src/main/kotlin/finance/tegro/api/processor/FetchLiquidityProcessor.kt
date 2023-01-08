package finance.tegro.api.processor

import finance.tegro.api.contract.WalletContract
import finance.tegro.api.entity.Liquidity
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.block.AddrStd
import org.ton.lite.client.LiteClient
import java.time.Instant

@Component
class FetchLiquidityProcessor(
    private val liteClient: LiteClient,
) : ItemProcessor<Pair<TonNodeBlockIdExt, Liquidity>, Liquidity> {
    override fun process(item: Pair<TonNodeBlockIdExt, Liquidity>): Liquidity {
        val (block, liquidity) = item

        val wallet = runBlocking {
            WalletContract.of(
                checkNotNull(liquidity.address as? AddrStd) { "Liquidity address is not valid" },
                liteClient,
                block
            )
        }

        return liquidity.apply {
            this.owner = wallet.owner
            this.exchangePair = wallet.jetton
            this.balance = wallet.balance
            this.block = block
            this.timestamp = Instant.now()
        }
    }

    companion object : KLogging()
}
