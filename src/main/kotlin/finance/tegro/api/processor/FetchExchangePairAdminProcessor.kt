package finance.tegro.api.processor

import finance.tegro.api.contract.PairContract
import finance.tegro.api.entity.ExchangePairAdmin
import finance.tegro.api.repository.ExchangePairAdminRepository
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
class FetchExchangePairAdminProcessor(
    private val liteClient: LiteClient,
    private val exchangePairAdminRepository: ExchangePairAdminRepository,
) : ItemProcessor<Pair<TonNodeBlockIdExt, MsgAddress>, ExchangePairAdmin> {
    override fun process(item: Pair<TonNodeBlockIdExt, MsgAddress>): ExchangePairAdmin {
        val (block, address) = item

        val admin = runBlocking {
            PairContract.getAdminAddress(
                checkNotNull(address as? AddrStd) { "ExchangePairAdmin address is not valid" },
                liteClient,
                block
            )
        }

        return exchangePairAdminRepository.findByAddress(address).orElse(null)
            ?.apply {
                this.admin = admin
                this.block = block
                this.timestamp = Instant.now()
            }
            ?: ExchangePairAdmin(
                address,
                admin,
                block,
                Instant.now()
            )
    }

    companion object : KLogging()
}
