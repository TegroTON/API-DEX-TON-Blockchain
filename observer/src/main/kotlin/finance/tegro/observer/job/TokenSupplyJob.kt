package finance.tegro.observer.job

import finance.tegro.core.entity.BlockId
import finance.tegro.core.entity.TokenSupply
import finance.tegro.core.repository.TokenContractRepository
import finance.tegro.core.repository.TokenRepository
import finance.tegro.core.repository.TokenSupplyRepository
import finance.tegro.core.toSafeString
import mu.KLogging
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component
import org.ton.block.MsgAddress
import java.time.Instant

@Component
class TokenSupplyJob(
    private val tokenContractRepository: TokenContractRepository,
    private val tokenSupplyRepository: TokenSupplyRepository,
    private val tokenRepository: TokenRepository,
) : Job {
    override fun execute(context: JobExecutionContext) {
        val jobData = context.mergedJobDataMap
        val address = jobData["address"] as MsgAddress
        val blockId = jobData["blockId"] as BlockId

        logger.debug { "started for address ${address.toSafeString()} and blockId ${blockId.workchain}:${blockId.shard}:${blockId.seqno}" }

        val tokenContract = tokenContractRepository.findByAddress(address).orElse(null) ?: return

        tokenSupplyRepository.save(
            TokenSupply(
                address,
                tokenContract.totalSupply,
                blockId,
                Instant.now()
            ).apply {
                token = tokenRepository.findByAddress(address).orElse(null)
            }
        ).also { logger.debug { "TokenSupply ${it.address.toSafeString()} of ${it.supply} was created" } }
    }

    companion object : KLogging()
}
