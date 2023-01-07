package finance.tegro.api.service

import finance.tegro.api.loadTransaction
import finance.tegro.api.repository.ExchangePairRepository
import io.awspring.cloud.messaging.listener.annotation.SqsListener
import mu.KLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.messaging.Message
import org.springframework.stereotype.Service
import org.ton.block.IntMsgInfo
import org.ton.lite.api.liteserver.LiteServerTransactionInfo

@Service
class TransactionService(
    private val exchangePairRepository: ExchangePairRepository,

    private val jobLauncher: JobLauncher,
    private val updateReserveJob: Job,
) {
    @SqsListener("transactions")
    fun onTransaction(message: Message<LiteServerTransactionInfo>) {
        val transactionInfo = message.payload

        val id = transactionInfo.id
        val transaction = transactionInfo.loadTransaction()

        logger.info("Received transaction $id:${transaction.lt}")

        if (transaction.in_msg.value == null)
            return // Ignore weird ass messages

        when (val info = requireNotNull(transaction.in_msg.value).info) {
            is IntMsgInfo -> {
                if (info.bounced)
                    return // Ignore bounced messages

                if (!exchangePairRepository.existsByAddress(info.dest))
                    return // Ignore messages not to exchange pairs

                // Trigger reserve update
                jobLauncher.run(updateReserveJob, JobParameters()).also {
                    logger.info { "$id triggered reserve update because of an internal message to ${info.dest}" }
                }
            }

            else -> return

        }
    }

    companion object : KLogging()
}
