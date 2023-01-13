package finance.tegro.observer.job

import finance.tegro.core.contract.PairContract
import finance.tegro.core.entity.BlockId
import finance.tegro.core.entity.ExchangePairToken
import finance.tegro.core.repository.ExchangePairRepository
import finance.tegro.core.repository.ExchangePairTokenRepository
import finance.tegro.core.toSafeString
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.quartz.*
import org.springframework.stereotype.Component
import org.ton.block.AddrStd
import org.ton.lite.client.LiteClient
import java.time.Instant

@Component
class ExchangePairTokenJob(
    private val scheduler: Scheduler,
    private val liteClient: LiteClient,

    private val exchangePairRepository: ExchangePairRepository,
    private val exchangePairTokenRepository: ExchangePairTokenRepository,
) : Job {
    override fun execute(context: JobExecutionContext) {
        val jobData = context.mergedJobDataMap
        val address = jobData["address"] as AddrStd
        val blockId = jobData["blockId"] as BlockId

        logger.debug { "ExchangePairTokenJob started for address ${address.toSafeString()} and blockId ${blockId.workchain}:${blockId.shard}:${blockId.seqno}" }

        if (!exchangePairRepository.existsByAddress(address))
            return

        val (base, quote) = runBlocking {
            PairContract.getPairTokens(
                checkNotNull(address as? AddrStd) { "ExchangePairToken address is not valid" },
                liteClient,
                blockId.toTonNodeBlockIdExt()
            )
        }

        val exchangePairToken = exchangePairTokenRepository.findByAddress(address).orElse(null)
            ?.apply {
                this.base = base
                this.quote = quote
                this.blockId = blockId
                this.timestamp = Instant.now()
            }
            ?: exchangePairTokenRepository.save(
                ExchangePairToken(
                    address,
                    base,
                    quote,
                    blockId,
                    Instant.now()
                )
            ).also {
                logger.info { "ExchangePairToken ${it.address.toSafeString()} was created" }
            }

        exchangePairRepository.updateTokenByAddress(exchangePairToken, exchangePairToken.address)

        for (token in listOf(address, base, quote)) { // Exchange pair contract itself is an LP token master
            val tokenJobKey = JobKey("TokenJob_${token.toSafeString()}_${blockId.id}", "TokenJob")
            if (!scheduler.checkExists(tokenJobKey))
                scheduler.scheduleJob(
                    JobBuilder.newJob(TokenJob::class.java)
                        .withIdentity(tokenJobKey)
                        .usingJobData(
                            JobDataMap(
                                mapOf(
                                    "address" to token,
                                    "blockId" to blockId
                                )
                            )
                        )
                        .build(),
                    TriggerBuilder.newTrigger()
                        .withIdentity("TokenTrigger_${token.toSafeString()}_${blockId.id}")
                        .startNow()
                        .build()
                )
        }
    }

    companion object : KLogging()
}
