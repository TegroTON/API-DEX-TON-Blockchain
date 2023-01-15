package finance.tegro.observer.job

import finance.tegro.core.contract.PairContract
import finance.tegro.core.entity.BlockId
import finance.tegro.core.entity.ExchangePairAdmin
import finance.tegro.core.repository.ExchangePairAdminRepository
import finance.tegro.core.repository.ExchangePairRepository
import finance.tegro.core.toSafeString
import kotlinx.coroutines.*
import mu.KLogging
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component
import org.ton.block.AddrStd
import org.ton.block.MsgAddress
import org.ton.lite.client.LiteClient
import java.time.Instant

@Component
class ExchangePairAdminJob(
    private val liteClient: LiteClient,
    private val exchangePairRepository: ExchangePairRepository,
    private val exchangePairAdminRepository: ExchangePairAdminRepository,
) : Job, CoroutineScope by CoroutineScope(Dispatchers.IO + CoroutineName("ExchangePairAdminJob")) {
    override fun execute(context: JobExecutionContext) {
        val jobData = context.mergedJobDataMap
        val address = jobData["address"] as MsgAddress
        val blockId = jobData["blockId"] as BlockId

        logger.debug { "started for address ${address.toSafeString()} and blockId ${blockId.workchain}:${blockId.shard}:${blockId.seqno}" }

        if (!exchangePairRepository.existsByAddress(address))
            return

        launch {
            val admin = PairContract.getAdminAddress(
                checkNotNull(address as? AddrStd) { "ExchangePairAdmin address is not valid" },
                liteClient,
                blockId.toTonNodeBlockIdExt()
            )

            val exchangePairAdmin = (
                    withContext(Dispatchers.IO) {
                        exchangePairAdminRepository.findByAddress(address)
                    }.orElse(null)
                        ?.apply {
                            this.admin = admin
                            this.blockId = blockId
                            this.timestamp = Instant.now()
                        }
                        ?: ExchangePairAdmin(
                            address,
                            admin,
                            blockId,
                            Instant.now()
                        ).also {
                            logger.info { "ExchangePairAdmin ${it.address.toSafeString()} was created" }
                        })
                .let {
                    exchangePairAdminRepository.save(it)
                }

            withContext(Dispatchers.IO) {
                exchangePairRepository.updateAdminByAddress(exchangePairAdmin, exchangePairAdmin.address)
            }
        }
    }

    companion object : KLogging()
}
