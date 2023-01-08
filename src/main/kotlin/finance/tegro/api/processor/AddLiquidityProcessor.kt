package finance.tegro.api.processor

import finance.tegro.api.contract.JettonContract
import finance.tegro.api.entity.Liquidity
import finance.tegro.api.repository.LiquidityRepository
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.block.AddrStd
import org.ton.block.MsgAddress
import org.ton.lite.client.LiteClient
import java.math.BigInteger
import java.time.Instant

@Component
class AddLiquidityProcessor(
    private val liteClient: LiteClient,
    private val liquidityRepository: LiquidityRepository,
) : ItemProcessor<Pair<TonNodeBlockIdExt, Pair<MsgAddress, MsgAddress>>, Liquidity> {
    override fun process(item: Pair<TonNodeBlockIdExt, Pair<MsgAddress, MsgAddress>>): Liquidity {
        val (block, ownerAndExchangePair) = item
        val (owner, exchangePair) = ownerAndExchangePair

        liquidityRepository.findByOwnerAndExchangePair(owner, exchangePair).orElse(null)?.run {
            return this // Return existing liquidity
        }

        val walletAddress = runBlocking {
            JettonContract.getWalletAddress(
                checkNotNull(exchangePair as? AddrStd) { "Exchange pair address is not valid" },
                owner,
                liteClient,
                block
            )
        }

        return Liquidity(
            address = walletAddress,
            owner = owner,
            exchangePair = exchangePair,
            balance = BigInteger.ZERO,
            block = block,
            timestamp = Instant.now()
        )
    }

    companion object : KLogging()
}
