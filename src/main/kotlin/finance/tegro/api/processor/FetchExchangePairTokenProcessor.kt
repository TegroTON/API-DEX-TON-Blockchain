package finance.tegro.api.processor

import finance.tegro.api.contract.PairContract
import finance.tegro.api.entity.ExchangePairToken
import finance.tegro.api.repository.ExchangePairTokenRepository
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
class FetchExchangePairTokenProcessor(
    private val liteClient: LiteClient,
    private val exchangePairTokenRepository: ExchangePairTokenRepository,
) : ItemProcessor<Pair<TonNodeBlockIdExt, MsgAddress>, ExchangePairToken> {
    override fun process(item: Pair<TonNodeBlockIdExt, MsgAddress>): ExchangePairToken {
        val (block, address) = item

        val (base, quote) = runBlocking {
            PairContract.getPairTokens(
                checkNotNull(address as? AddrStd) { "ExchangePairToken address is not valid" },
                liteClient,
                block
            )
        }

        return exchangePairTokenRepository.findByAddress(address).orElse(null)
            ?.apply {
                this.base = base
                this.quote = quote
                this.block = block
                this.timestamp = Instant.now()
            }
            ?: ExchangePairToken(
                address,
                base,
                quote,
                block,
                Instant.now()
            )
    }

    companion object : KLogging()
}
