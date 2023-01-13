package finance.tegro.observer.job

import finance.tegro.core.entity.BlockId
import finance.tegro.core.entity.Token
import finance.tegro.core.repository.ExchangePairTokenRepository
import finance.tegro.core.repository.TokenRepository
import finance.tegro.core.toSafeString
import mu.KLogging
import org.quartz.*
import org.springframework.stereotype.Component
import org.ton.block.MsgAddress
import java.time.Instant

@Component
class TokenJob(
    private val scheduler: Scheduler,

    private val exchangePairTokenRepository: ExchangePairTokenRepository,
    private val tokenRepository: TokenRepository,
) : Job {
    override fun execute(context: JobExecutionContext) {
        val jobData = context.mergedJobDataMap
        val address = jobData["address"] as MsgAddress
        val blockId = jobData["blockId"] as BlockId

        logger.debug { "started for address ${address.toSafeString()} and blockId ${blockId.workchain}:${blockId.shard}:${blockId.seqno}" }

        val token = tokenRepository.findByAddress(address).orElse(null)
            ?: tokenRepository.save(
                Token(
                    address,
                    Instant.now(),
                )
            ).also { logger.debug { "Token ${it.address.toSafeString()} was created" } }

        // Set up necessary relationships
        exchangePairTokenRepository.updateLiquidityTokenByAddress(token, token.address)
        exchangePairTokenRepository.updateBaseTokenByBase(token, token.address)
        exchangePairTokenRepository.updateQuoteTokenByQuote(token, token.address)

        val tokenContractJobKey =
            JobKey("TokenContractJob_${token.address.toSafeString()}_${blockId.id}", "TokenContractJob")

        if (!scheduler.checkExists(tokenContractJobKey))
            scheduler.scheduleJob(
                JobBuilder.newJob(TokenContractJob::class.java)
                    .withIdentity(tokenContractJobKey)
                    .usingJobData(
                        JobDataMap(
                            mapOf(
                                "address" to token.address,
                                "blockId" to blockId
                            )
                        )
                    )
                    .build(),
                TriggerBuilder.newTrigger()
                    .withIdentity(
                        "TokenContractTrigger_${token.address.toSafeString()}_${blockId.id}",
                        "TokenContractTrigger"
                    )
                    .startNow()
                    .build()
            )
    }

    companion object : KLogging()
}
