package finance.tegro.observer.job

import finance.tegro.core.contract.JettonContract
import finance.tegro.core.entity.BlockId
import finance.tegro.core.entity.TokenContract
import finance.tegro.core.repository.TokenContractRepository
import finance.tegro.core.repository.TokenRepository
import finance.tegro.core.toSafeString
import kotlinx.coroutines.*
import mu.KLogging
import org.quartz.*
import org.quartz.Job
import org.springframework.stereotype.Component
import org.ton.block.AddrNone
import org.ton.block.AddrStd
import org.ton.block.MsgAddress
import org.ton.lite.client.LiteClient
import java.time.Instant

@Component
class TokenContractJob(
    private val scheduler: Scheduler,
    private val liteClient: LiteClient,

    private val tokenContractRepository: TokenContractRepository,
    private val tokenRepository: TokenRepository,
) : Job, CoroutineScope by CoroutineScope(Dispatchers.IO + CoroutineName("TokenContractJob")) {
    override fun execute(context: JobExecutionContext) {
        val jobData = context.mergedJobDataMap
        val address = jobData["address"] as MsgAddress
        val blockId = jobData["blockId"] as BlockId

        logger.debug { "started for address ${address.toSafeString()} and blockId ${blockId.workchain}:${blockId.shard}:${blockId.seqno}" }

        launch {
            val contract = if (address != AddrNone) {
                JettonContract.of(
                    checkNotNull(address as? AddrStd) { "TokenContract address is not valid" },
                    liteClient,
                    blockId.toTonNodeBlockIdExt()
                )
            } else {
                JettonContract.ofTon()
            }

            val tokenContract = (withContext(Dispatchers.IO) {
                tokenContractRepository.findByAddress(address)
            }.orElse(null)
                ?.apply {
                    this.totalSupply = contract.totalSupply
                    this.mintable = contract.mintable
                    this.admin = contract.admin
                    this.content = contract.content
                    this.walletCode = contract.walletCode
                    this.blockId = blockId
                    this.timestamp = Instant.now()
                }
                ?: TokenContract(
                    address,
                    contract.totalSupply,
                    contract.mintable,
                    contract.admin,
                    contract.content,
                    contract.walletCode,
                    blockId,
                    Instant.now()
                )
                    .also {
                        logger.info { "TokenContract ${it.address.toSafeString()} was created" }
                    })
                .let {
                    tokenContractRepository.save(it)
                }

            withContext(Dispatchers.IO) {
                tokenRepository.updateContractByAddress(tokenContract, tokenContract.address)
            }

            val tokenMetadataJobKey =
                JobKey("TokenMetadataJob_${tokenContract.address.toSafeString()}_${blockId.id}", "TokenMetadataJob")

            if (!scheduler.checkExists(tokenMetadataJobKey))
                scheduler.scheduleJob(
                    JobBuilder.newJob(TokenMetadataJob::class.java)
                        .withIdentity(tokenMetadataJobKey)
                        .usingJobData(
                            JobDataMap(
                                mapOf(
                                    "address" to tokenContract.address,
                                    "blockId" to blockId
                                )
                            )
                        )
                        .build(),
                    TriggerBuilder.newTrigger()
                        .withIdentity("TokenMetadataTrigger_${tokenContract.address.toSafeString()}_${blockId.id}")
                        .startNow()
                        .build()
                )

            val tokenSupplyJobKey =
                JobKey("TokenSupplyJob_${tokenContract.address.toSafeString()}_${blockId.id}", "TokenSupplyJob")

            if (!scheduler.checkExists(tokenSupplyJobKey))
                scheduler.scheduleJob(
                    JobBuilder.newJob(TokenSupplyJob::class.java)
                        .withIdentity(tokenSupplyJobKey)
                        .usingJobData(
                            JobDataMap(
                                mapOf(
                                    "address" to tokenContract.address,
                                    "blockId" to blockId
                                )
                            )
                        )
                        .build(),
                    TriggerBuilder.newTrigger()
                        .withIdentity("TokenSupplyTrigger_${tokenContract.address.toSafeString()}_${blockId.id}")
                        .startNow()
                        .build()
                )
        }
    }

    companion object : KLogging()
}
