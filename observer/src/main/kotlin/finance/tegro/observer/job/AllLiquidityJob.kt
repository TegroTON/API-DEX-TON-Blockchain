package finance.tegro.observer.job

import finance.tegro.core.repository.BlockIdRepository
import finance.tegro.core.repository.ExchangePairRepository
import finance.tegro.core.repository.LiquidityRepository
import finance.tegro.core.toSafeString
import org.quartz.*
import org.springframework.stereotype.Component

@Component
class AllLiquidityJob(
    private val scheduler: Scheduler,
    private val blockIdRepository: BlockIdRepository,
    private val liquidityRepository: LiquidityRepository,
    private val exchangePairRepository: ExchangePairRepository,
) : Job {
    override fun execute(context: JobExecutionContext) {
        val blockId = blockIdRepository.findFirstByWorkchainOrderByTimestampDesc(-1).orElse(null) ?: return

        liquidityRepository.findDistinctOwners().forEach { owner ->
            exchangePairRepository.findAll().forEach { exchangePair ->
                val liquidityJobKey =
                    JobKey(
                        "LiquidityJob_${owner.toSafeString()}_${exchangePair.address.toSafeString()}_${blockId.id}",
                        "LiquidityJob"
                    )

                if (!scheduler.checkExists(liquidityJobKey)) {
                    scheduler.scheduleJob(
                        JobBuilder.newJob(LiquidityJob::class.java)
                            .withIdentity(liquidityJobKey)
                            .usingJobData(
                                JobDataMap(
                                    mapOf(
                                        "owner" to owner,
                                        "exchangePair" to exchangePair.address,
                                        "blockId" to blockId
                                    )
                                )
                            )
                            .build(),
                        TriggerBuilder.newTrigger()
                            .withIdentity(
                                "LiquidityTrigger_${owner.toSafeString()}_${exchangePair.address.toSafeString()}_${blockId.id}",
                                "LiquidityTrigger"
                            )
                            .startNow()
                            .build()
                    )
                }
            }
        }
    }
}
