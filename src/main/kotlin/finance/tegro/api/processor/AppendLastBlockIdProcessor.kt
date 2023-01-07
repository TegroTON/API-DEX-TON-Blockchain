package finance.tegro.api.processor

import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.lite.client.LiteClient

@Component
class AppendLastBlockIdProcessor<T : Any>(
    private val liteClient: LiteClient,
) : ItemProcessor<T, Pair<TonNodeBlockIdExt, T>> {
    override fun process(item: T): Pair<TonNodeBlockIdExt, T> = runBlocking {
        (liteClient.getLastBlockId() to item)
    }

    companion object : KLogging()
}
