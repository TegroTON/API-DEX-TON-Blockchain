package finance.tegro.rest.v2.services

import finance.tegro.tonindexer.services.MasterchainBlockService
import finance.tegro.tonindexer.services.TonLiteApiService
import io.ktor.util.collections.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.bitstring.Bits256
import org.ton.crypto.sha256
import org.ton.lite.api.LiteApi
import org.ton.lite.api.liteserver.LiteServerAccountId
import org.ton.lite.api.liteserver.functions.LiteServerGetAccountState
import kotlin.coroutines.CoroutineContext

object AccountStatesService : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineName(toString())

    private lateinit var job: Job
    private val accountStateFlows =
        ConcurrentMap<LiteServerAccountId, CompletableDeferred<MutableStateFlow<Pair<TonNodeBlockIdExt, Bits256>>>>()

    fun init(
        liteApi: LiteApi = TonLiteApiService.liteApi,
        masterchainBlockIdFlow: StateFlow<TonNodeBlockIdExt> = MasterchainBlockService.blockIdFlow,
    ) {
        job = launch {
            masterchainBlockIdFlow.collectLatest { mcBlockId ->
                val accountStates = accountStateFlows.keys.map { address ->
                    async {
                        address to liteApi(
                            LiteServerGetAccountState(
                                mcBlockId,
                                address
                            )
                        ).state.let {
                            Bits256(sha256(it))
                        }
                    }
                }.awaitAll()
                accountStates.forEach { (address, stateHash) ->
                    updateStateFlow(mcBlockId, address, stateHash)
                }
            }
        }
    }

    suspend fun stateFlow(address: LiteServerAccountId) = accountStateFlows.getOrPut(
        address,
        defaultValue = { CompletableDeferred() }
    ).await().asStateFlow().map {
        it.first
    }

    fun removeStateFlow(address: LiteServerAccountId) {
        accountStateFlows.remove(address)
    }

    private suspend fun updateStateFlow(
        mcBlockId: TonNodeBlockIdExt,
        address: LiteServerAccountId,
        stateHash: Bits256
    ) {
        val currentState = accountStateFlows[address] ?: return
        if (!currentState.isCompleted) {
            currentState.complete(MutableStateFlow(mcBlockId to stateHash))
        } else {
            val flow = currentState.await()
            if (flow.value.second != stateHash) {
                flow.update {
                    mcBlockId to stateHash
                }
            }
        }
    }
}
