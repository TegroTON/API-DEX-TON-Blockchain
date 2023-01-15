package finance.tegro.observer.job

import finance.tegro.core.contract.PairContract
import finance.tegro.core.entity.BlockId
import finance.tegro.core.entity.Reserve
import finance.tegro.core.repository.ExchangePairRepository
import finance.tegro.core.repository.ReserveRepository
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
class ReserveJob(
    private val liteClient: LiteClient,

    private val exchangePairRepository: ExchangePairRepository,
    private val reserveRepository: ReserveRepository,
) : Job, CoroutineScope by CoroutineScope(Dispatchers.IO + CoroutineName("ReserveJob")) {
    override fun execute(context: JobExecutionContext) {
        val jobData = context.mergedJobDataMap
        val address = jobData["address"] as MsgAddress
        val blockId = jobData["blockId"] as BlockId

        logger.debug { "started for address ${address.toSafeString()} and blockId ${blockId.workchain}:${blockId.shard}:${blockId.seqno}" }

        if (!exchangePairRepository.existsByAddress(address))
            return

        launch {
            val (base, quote) = PairContract.getReserves(
                checkNotNull(address as? AddrStd) { "Reserve address is not valid" },
                liteClient,
                blockId.toTonNodeBlockIdExt()
            )

            withContext(Dispatchers.IO) {
                reserveRepository.save(
                    Reserve(
                        address,
                        base,
                        quote,
                        blockId,
                        Instant.now()
                    ).apply {
                        this.exchangePair = exchangePairRepository.findByAddress(address).orElse(null)
                    }
                )
            }.also {
                logger.debug { "Reserve ${it.address.toSafeString()}: ${it.base}/${it.quote} was added" }
            }
        }
    }

    companion object : KLogging()
}
