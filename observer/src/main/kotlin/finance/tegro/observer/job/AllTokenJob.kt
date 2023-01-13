package finance.tegro.observer.job

import finance.tegro.core.repository.BlockIdRepository
import finance.tegro.core.repository.TokenRepository
import finance.tegro.core.toSafeString
import mu.KLogging
import org.quartz.*
import org.springframework.stereotype.Component

@Component
class AllTokenJob(
    private val scheduler: Scheduler,

    private val blockIdRepository: BlockIdRepository,
    private val tokenRepository: TokenRepository,
) : Job {
    override fun execute(context: JobExecutionContext) {
        val blockId = blockIdRepository.findFirstByWorkchainOrderByTimestampDesc(-1).orElse(null) ?: return
        logger.debug { "started for blockId ${blockId.workchain}:${blockId.shard}:${blockId.seqno}" }

        for (token in tokenRepository.findAll()) {
            val tokenJobKey =
                JobKey("TokenJob_${token.address.toSafeString()}_${blockId.id}", "TokenJob")

            if (!scheduler.checkExists(tokenJobKey))
                scheduler.scheduleJob(
                    JobBuilder.newJob(TokenJob::class.java)
                        .withIdentity(tokenJobKey)
                        .usingJobData(
                            JobDataMap(
                                mapOf(
                                    "address" to token.address,
                                    "blockId" to blockId,
                                )
                            )
                        )
                        .build(),
                    TriggerBuilder.newTrigger()
                        .withIdentity(
                            "TokenTrigger_${token.address.toSafeString()}_${blockId.id}",
                            "TokenTrigger"
                        )
                        .startNow()
                        .build()
                )
        }
    }

    companion object : KLogging()
}
