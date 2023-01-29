package finance.tegro.rest.v2.services

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.ton.api.tonnode.TonNodeBlockId
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.lite.api.LiteApi
import org.ton.lite.api.liteserver.functions.LiteServerGetMasterchainInfo
import org.ton.lite.api.liteserver.functions.LiteServerLookupBlock
import kotlin.coroutines.CoroutineContext

@OptIn(DelicateCoroutinesApi::class)
object MasterchainBlockService : CoroutineScope {
    override val coroutineContext: CoroutineContext = newSingleThreadContext(toString()) + CoroutineName(toString())

    private lateinit var _blockIdFlow: MutableStateFlow<TonNodeBlockIdExt>
    private lateinit var liteApi: LiteApi
    private lateinit var job: Job

    lateinit var blockIdFlow: StateFlow<TonNodeBlockIdExt>
        private set

    fun init(liteApi: LiteApi = TonLiteApiService.liteApi) {
        this.liteApi = liteApi
        val initBlockId = runBlocking {
            liteApi(LiteServerGetMasterchainInfo).last
        }
        _blockIdFlow = MutableStateFlow(initBlockId)
        blockIdFlow = _blockIdFlow.asStateFlow()
        this.job = launch {
            var blockId = initBlockId
            while (true) {
                try {
                    blockId = lookupBlockId(liteApi, blockId.workchain, blockId.shard, blockId.seqno + 1)
                    _blockIdFlow.update {
                        blockId
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    delay(1000)
                    println("${this@MasterchainBlockService} failed to get masterchain block: $e")
                }
            }
        }
    }

    private suspend fun lookupBlockId(liteApi: LiteApi, workchain: Int, shard: Long, seqno: Int): TonNodeBlockIdExt {
        return liteApi(
            LiteServerLookupBlock(
                LiteServerLookupBlock.ID_MASK,
                TonNodeBlockId(workchain, shard, seqno),
                null,
                null
            ), seqno
        ).id
    }
}
