package finance.tegro.observer.service

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import mu.KLogging
import org.springframework.stereotype.Service
import org.ton.block.ExtInMsgInfo
import org.ton.block.ExtOutMsgInfo
import org.ton.block.IntMsgInfo

@Service
class TransactionService(
    private val masterchainBlockService: MasterchainBlockService,
    private val shardchainBlockService: ShardchainBlockService,
) {
    @OptIn(FlowPreview::class)
    val data = merge(masterchainBlockService.data, shardchainBlockService.data)
        .flatMapConcat { (id, block) ->
            block.extra.account_blocks.nodes()
                .flatMap { (account, _) ->
                    account.transactions.nodes().map { (transaction, _) -> id to transaction }
                }
                .asFlow()
        }
        .onEach { (_, transaction) ->
            when (val info = transaction.in_msg.value?.info) {
                is ExtInMsgInfo -> {
                    logger.trace { "${info.src} -> (ext in) -> ${info.dest}" }
                }

                is ExtOutMsgInfo -> {
                    logger.trace { "${info.src} -> (ext out) -> ${info.dest}" }
                }

                is IntMsgInfo -> {
                    logger.trace { "${info.src} -> (in) -> ${info.dest}" }
                }

                null -> {}
            }
        }
        .shareIn(CoroutineScope(Dispatchers.IO + CoroutineName("TransactionService")), SharingStarted.Eagerly, 512)

    companion object : KLogging()
}
