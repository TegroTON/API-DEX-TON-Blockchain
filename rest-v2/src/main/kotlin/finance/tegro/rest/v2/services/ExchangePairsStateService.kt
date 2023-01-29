@file:OptIn(DelicateCoroutinesApi::class)

package finance.tegro.rest.v2.services

import finance.tegro.rest.v2.exchangePairsFacade
import finance.tegro.rest.v2.models.ExchangePairFacade
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

object ExchangePairsStateService : CoroutineScope {
    override val coroutineContext: CoroutineContext = newSingleThreadContext(toString()) + CoroutineName(toString())

    private lateinit var job: Job
    private val exchangePairsStatesFlows =
        ConcurrentMap<LiteServerAccountId, CompletableDeferred<MutableStateFlow<Pair<TonNodeBlockIdExt, Bits256>>>>()

    fun init(
        liteApi: LiteApi = TonLiteApiService.liteApi,
        exchangePairFacade: ExchangePairFacade = exchangePairsFacade,
        masterchainBlockIdFlow: StateFlow<TonNodeBlockIdExt> = MasterchainBlockService.blockIdFlow,
    ) {
        job = launch {
            masterchainBlockIdFlow.collectLatest { mcBlockId ->
                val exchangePairsStates = exchangePairFacade.allExchangePairs().map { exchangePair ->
                    async {
                        exchangePair.address to liteApi(
                            LiteServerGetAccountState(
                                mcBlockId,
                                exchangePair.address
                            )
                        ).state.let {
                            Bits256(sha256(it))
                        }
                    }
                }.awaitAll()
                exchangePairsStates.forEach { (address, stateHash) ->
                    updateStateFlow(mcBlockId, address, stateHash)
                }
            }
        }
    }

    suspend fun stateFlow(address: LiteServerAccountId) = exchangePairsStatesFlows.getOrPut(
        address,
        defaultValue = { CompletableDeferred() }
    ).await().asStateFlow().map {
        it.first
    }

    private suspend fun updateStateFlow(
        mcBlockId: TonNodeBlockIdExt,
        address: LiteServerAccountId,
        stateHash: Bits256
    ) {
        val currentState = exchangePairsStatesFlows.getOrPut(
            address,
            defaultValue = { CompletableDeferred() }
        )
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
