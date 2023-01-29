package finance.tegro.rest.v2.services

import finance.tegro.rest.v2.exchangePairsFacade
import finance.tegro.rest.v2.models.ExchangePair
import finance.tegro.rest.v2.models.ExchangePairFacade
import finance.tegro.rest.v2.models.Reserves
import finance.tegro.rest.v2.utils.smcCreateParams
import finance.tegro.rest.v2.utils.smcMethodId
import io.ktor.util.collections.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.block.VmStack
import org.ton.block.VmStackList
import org.ton.boc.BagOfCells
import org.ton.lite.api.LiteApi
import org.ton.lite.api.liteserver.LiteServerAccountId
import org.ton.lite.api.liteserver.functions.LiteServerRunSmcMethod
import kotlin.coroutines.CoroutineContext

object ReservesService : CoroutineScope {
    @OptIn(DelicateCoroutinesApi::class)
    override val coroutineContext: CoroutineContext = newSingleThreadContext(toString()) + CoroutineName(toString())

    private lateinit var job: Job
    private val stateFlows = ConcurrentMap<LiteServerAccountId, CompletableDeferred<MutableStateFlow<Reserves>>>()

    fun init(
        liteApi: LiteApi = TonLiteApiService.liteApi,
        exchangePairs: ExchangePairFacade = exchangePairsFacade,
    ) {
        job = launch {
            while (true) {
                exchangePairs.allExchangePairs().forEach { exchangePair ->
                    val reservesStateFlow = stateFlows.getOrPut(exchangePair.address) {
                        CompletableDeferred()
                    }
                    if (!reservesStateFlow.isCompleted) {
                        launch {
                            reservesStateFlow.complete(
                                MutableStateFlow(
                                    getReserves(
                                        liteApi,
                                        MasterchainBlockService.blockIdFlow.value,
                                        exchangePair.address
                                    )!!
                                )
                            )
                            ExchangePairsStateService.stateFlow(exchangePair.address).collectLatest { mcBlockId ->
                                val reserves =
                                    getReserves(liteApi, mcBlockId, exchangePair.address) ?: return@collectLatest
                                reservesStateFlow.await().update {
                                    reserves
                                }
                            }
                        }
                    }
                }
                delay(1000)
            }
        }
    }

    suspend fun reserves(exchangePair: ExchangePair): StateFlow<Reserves> =
        stateFlows.getOrPut(exchangePair.address) { CompletableDeferred() }.await().asStateFlow()

    suspend fun reservesAll(): List<Reserves> = coroutineScope {
        exchangePairsFacade.allExchangePairs().map { exchangePair ->
            async {
                reserves(exchangePair).value
            }
        }.awaitAll().sortedByDescending {
            it.base
        }
    }

    private suspend fun getReserves(
        liteApi: LiteApi,
        mcBlockId: TonNodeBlockIdExt,
        address: LiteServerAccountId
    ): Reserves? {
        val result = liteApi(
            LiteServerRunSmcMethod(
                0b100,
                mcBlockId,
                address,
                smcMethodId("get::reserves"),
                smcCreateParams(VmStack(VmStackList())).toByteArray()
            )
        ).result ?: return null
        val stackValues = VmStack.loadTlb(BagOfCells(result).first()).toMutableVmStack()
        return Reserves(
            address = address,
            base = stackValues.popInt(),
            quote = stackValues.popInt()
        ).also {
            println("${Clock.System.now()} new reserves: $it")
        }
    }
}
