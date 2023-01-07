package finance.tegro.api.processor

import finance.tegro.api.contract.PairContract
import finance.tegro.api.entity.Reserve
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
class FetchReserveProcessor(
    private val liteClient: LiteClient,
) : ItemProcessor<Pair<TonNodeBlockIdExt, MsgAddress>, Reserve> {
    override fun process(item: Pair<TonNodeBlockIdExt, MsgAddress>): Reserve {
        val (block, address) = item

        val (base, quote) = runBlocking {
            PairContract.getReserves(
                checkNotNull(address as? AddrStd) { "Reserve address is not valid" },
                liteClient,
                block
            )
        }

        return Reserve(
            address,
            base,
            quote,
            block,
            Instant.now()
        )
    }

    companion object : KLogging()
}
