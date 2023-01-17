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
import java.math.BigInteger
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
            val walletAddress = try {
                JettonContract.getWalletAddress(
                    checkNotNull(exchangePair as? AddrStd) { "Exchange pair address is not valid" },
                    owner,
                    liteClient,
                    blockId.toTonNodeBlockIdExt(),
                )
            } catch (e: Exception) {
                logger.warn(e) { "failed to get wallet address for exchange pair ${exchangePair.toSafeString()} and owner ${owner.toSafeString()} and blockId ${blockId.workchain}:${blockId.shard}:${blockId.seqno}" }
                null
            } ?: return@launch


            val wallet = try {
                WalletContract.of(
                    checkNotNull(walletAddress as? AddrStd) { "Liquidity wallet address is not valid" },
                    liteClient,
                    blockId.toTonNodeBlockIdExt(),
                )
            } catch (e: Exception) {
                logger.warn(e) { "failed to get wallet for address ${walletAddress.toSafeString()} and blockId ${blockId.workchain}:${blockId.shard}:${blockId.seqno}" }
                null
            }

            withContext(Dispatchers.IO) {
                liquidityRepository.save(
                    Liquidity(
                        walletAddress,
                        wallet?.owner ?: owner,
                        wallet?.jetton ?: exchangePair,
                        wallet?.balance ?: BigInteger.ZERO,
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
            val tokenJobKey = JobKey("TokenJob_${exchangePair.toSafeString()}_${blockId.id}", "TokenJob")

            if (!scheduler.checkExists(tokenJobKey))
                scheduler.scheduleJob(
                    JobBuilder.newJob(TokenJob::class.java)
                        .withIdentity(tokenJobKey)
                        .usingJobData(
                            JobDataMap(
                                mapOf(
                                    "address" to exchangePair,
                                    "blockId" to blockId,
                                )
                            )
                        )
                        .build(),
                    TriggerBuilder.newTrigger()
                        .withIdentity(
                            "TokenTrigger_${exchangePair.toSafeString()}_${blockId.id}",
                            "TokenTrigger"
                        )
                        .startNow()
                        .build()
                )
        }
    }

    companion object : KLogging()
}
