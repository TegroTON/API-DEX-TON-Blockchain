package finance.tegro.api.processor

import finance.tegro.api.contract.JettonContract
import finance.tegro.api.entity.TokenContract
import finance.tegro.api.repository.TokenContractRepository
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.block.AddrNone
import org.ton.block.AddrStd
import org.ton.block.MsgAddress
import org.ton.lite.client.LiteClient
import java.time.Instant

@Component
class FetchTokenContractProcessor(
    private val liteClient: LiteClient,
    private val tokenContractRepository: TokenContractRepository,
) : ItemProcessor<Pair<TonNodeBlockIdExt, MsgAddress>, TokenContract> {
    override fun process(item: Pair<TonNodeBlockIdExt, MsgAddress>): TokenContract {
        val (block, address) = item

        val tokenContract = if (address != AddrNone) {
            runBlocking {
                JettonContract.of(
                    checkNotNull(address as? AddrStd) { "TokenContract address is not valid" },
                    liteClient,
                    block
                )
            }
        } else {
            JettonContract.ofTon()
        }

        return tokenContractRepository.findByAddress(address).orElse(null)
            ?.apply {
                this.totalSupply = tokenContract.totalSupply
                this.mintable = tokenContract.mintable
                this.admin = tokenContract.admin
                this.content = tokenContract.content
                this.walletCode = tokenContract.walletCode
                this.block = block
                this.timestamp = Instant.now()
            }
            ?: TokenContract(
                address,
                tokenContract.totalSupply,
                tokenContract.mintable,
                tokenContract.admin,
                tokenContract.content,
                tokenContract.walletCode,
                block,
                Instant.now()
            )
    }

    companion object : KLogging()
}
