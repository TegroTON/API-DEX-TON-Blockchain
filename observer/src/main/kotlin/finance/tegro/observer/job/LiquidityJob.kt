package finance.tegro.observer.job

import finance.tegro.core.contract.JettonContract
import finance.tegro.core.contract.WalletContract
import finance.tegro.core.entity.BlockId
import finance.tegro.core.entity.Liquidity
import finance.tegro.core.repository.ExchangePairRepository
import finance.tegro.core.repository.LiquidityRepository
import finance.tegro.core.toSafeString
import kotlinx.coroutines.*
import mu.KLogging
import org.quartz.*
import org.quartz.Job
import org.springframework.stereotype.Component
import org.ton.block.AddrStd
import org.ton.block.MsgAddress
import org.ton.lite.client.LiteClient
import java.time.Instant

@Component
class LiquidityJob(
    private val scheduler: Scheduler,
    private val liteClient: LiteClient,

    private val liquidityRepository: LiquidityRepository,
    private val exchangePairRepository: ExchangePairRepository,
) : Job, CoroutineScope by CoroutineScope(Dispatchers.IO + CoroutineName("LiquidityJob")) {
    override fun execute(context: JobExecutionContext) {
        val jobData = context.mergedJobDataMap
        val owner = jobData["owner"] as MsgAddress
        val exchangePair = jobData["exchangePair"] as MsgAddress
        val blockId = jobData["blockId"] as BlockId

        launch {
            val walletAddress = JettonContract.getWalletAddress(
                checkNotNull(exchangePair as? AddrStd) { "Exchange pair address is not valid" },
                owner,
                liteClient,
                blockId.toTonNodeBlockIdExt(),
            )

            val wallet = WalletContract.of(
                checkNotNull(walletAddress as? AddrStd) { "Liquidity wallet address is not valid" },
                liteClient,
                blockId.toTonNodeBlockIdExt(),
            )

            withContext(Dispatchers.IO) {
                liquidityRepository.save(
                    Liquidity(
                        walletAddress,
                        wallet.owner,
                        wallet.jetton,
                        wallet.balance,
                        blockId,
                        Instant.now()
                    ).apply {
                        this.exchangePair = exchangePairRepository.findByAddress(exchangePairAddress).orElse(null)
                    }
                )
            }.also {
                logger.debug { "Liquidity ${it.address.toSafeString()} ${it.balance}LP of ${it.exchangePairAddress.toSafeString()} was created" }
            }

            // Trigger liquidity token update
            val tokenJobKey = JobKey("TokenJob_${wallet.jetton.toSafeString()}_${blockId.id}", "TokenJob")

            if (!scheduler.checkExists(tokenJobKey))
                scheduler.scheduleJob(
                    JobBuilder.newJob(TokenJob::class.java)
                        .withIdentity(tokenJobKey)
                        .usingJobData(
                            JobDataMap(
                                mapOf(
                                    "address" to wallet.jetton,
                                    "blockId" to blockId,
                                )
                            )
                        )
                        .build(),
                    TriggerBuilder.newTrigger()
                        .withIdentity(
                            "TokenTrigger_${wallet.jetton.toSafeString()}_${blockId.id}",
                            "TokenTrigger"
                        )
                        .startNow()
                        .build()
                )
        }
    }

    companion object : KLogging()
}
