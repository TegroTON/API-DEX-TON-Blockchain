package finance.tegro.rest.v2.services

import finance.tegro.rest.v2.exchangePairsFacade
import finance.tegro.rest.v2.models.ExchangePairFacade
import finance.tegro.rest.v2.models.PairJettons
import finance.tegro.rest.v2.utils.smcCreateParams
import finance.tegro.rest.v2.utils.smcMethodId
import finance.tegro.rest.v2.utils.toAccountId
import io.ktor.util.collections.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.block.MsgAddress
import org.ton.block.VmStack
import org.ton.block.VmStackList
import org.ton.boc.BagOfCells
import org.ton.lite.api.LiteApi
import org.ton.lite.api.liteserver.LiteServerAccountId
import org.ton.lite.api.liteserver.functions.LiteServerRunSmcMethod
import org.ton.tlb.loadTlb
import kotlin.coroutines.CoroutineContext

object PairsService : CoroutineScope {
    private val log = LoggerFactory.getLogger(PairsService::class.java)
    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineName(log.name)

    private lateinit var job: Job
    private val stateFlows = ConcurrentMap<LiteServerAccountId, CompletableDeferred<MutableStateFlow<PairJettons>>>()

    fun init(
        liteApi: LiteApi = TonLiteApiService.liteApi,
        exchangePairs: ExchangePairFacade = exchangePairsFacade,
    ) {
        job = launch {
            while (true) {
                exchangePairs.allExchangePairs().forEach { address ->
                    val pairJettonsStateFlow = stateFlows.getOrPut(address) {
                        CompletableDeferred()
                    }
                    if (!pairJettonsStateFlow.isCompleted) {
                        launch {
                            pairJettonsStateFlow.complete(
                                MutableStateFlow(
                                    getPairJettons(
                                        liteApi,
                                        MasterchainBlockService.blockIdFlow.value,
                                        address
                                    )!!
                                )
                            )
                            AccountStatesService.stateFlow(address).collectLatest { mcBlockId ->
                                val pairJettons =
                                    getPairJettons(liteApi, mcBlockId, address) ?: return@collectLatest
                                pairJettonsStateFlow.await().update {
                                    pairJettons
                                }
                            }
                        }
                    }
                }
                delay(5000)
            }
        }
    }

    suspend fun pairJettons(address: LiteServerAccountId): StateFlow<PairJettons> =
        stateFlows.getOrPut(address) { CompletableDeferred() }.await().asStateFlow()

    suspend fun pairsAll(): List<PairJettons> = coroutineScope {
        exchangePairsFacade.allExchangePairs().map { address ->
            async {
                pairJettons(address).value
            }
        }.awaitAll()
    }

    private suspend fun getPairJettons(
        liteApi: LiteApi,
        mcBlockId: TonNodeBlockIdExt,
        address: LiteServerAccountId
    ): PairJettons? {
        val result = liteApi(
            LiteServerRunSmcMethod(
                0b100,
                mcBlockId,
                address,
                smcMethodId("get::pair_tokens"),
                smcCreateParams(VmStack(VmStackList())).toByteArray()
            )
        ).result ?: return null
        val stackValues = VmStack.loadTlb(BagOfCells(result).first()).toMutableVmStack()

        return PairJettons(
            liquidity = address,
            base = stackValues.popSlice().loadTlb(MsgAddress).toAccountId(),
            quote = stackValues.popSlice().loadTlb(MsgAddress).toAccountId()
        ).also {
            log.info("new pairs: $it")
        }
    }
}
