package finance.tegro.rest.v2.services

import finance.tegro.rest.v2.models.JettonContent
import finance.tegro.rest.v2.models.JettonData
import finance.tegro.rest.v2.utils.smcCreateParams
import finance.tegro.rest.v2.utils.smcMethodId
import finance.tegro.rest.v2.utils.toAccountId
import finance.tegro.tonindexer.services.MasterchainBlockService
import finance.tegro.tonindexer.services.TonLiteApiService
import io.ktor.util.collections.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import org.slf4j.LoggerFactory
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.block.MsgAddressInt
import org.ton.block.VmStack
import org.ton.block.VmStackList
import org.ton.boc.BagOfCells
import org.ton.lite.api.LiteApi
import org.ton.lite.api.liteserver.LiteServerAccountId
import org.ton.lite.api.liteserver.functions.LiteServerRunSmcMethod
import kotlin.coroutines.CoroutineContext

object JettonDataService : CoroutineScope {
    private val log = LoggerFactory.getLogger(JettonDataService::class.java)
    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineName(log.name)
    private val stateFlows = ConcurrentMap<LiteServerAccountId, CompletableDeferred<MutableStateFlow<JettonData>>>()

    private lateinit var job: Job

    fun init(
        liteApi: LiteApi = TonLiteApiService.liteApi,
    ) {
        job = launch {
            MasterchainBlockService.blockIdFlow.collectLatest { mcBlockId ->
                PairsService.pairsAll().asSequence().map { listOf(it.liquidity, it.base, it.quote) }.flatten()
                    .filterNotNull()
                    .distinct().forEach { address ->
                        val state = stateFlows.getOrPut(address) {
                            CompletableDeferred()
                        }
                        if (!state.isCompleted) {
                            launch {
                                state.complete(
                                    MutableStateFlow(
                                        getJettonData(liteApi, mcBlockId, address)
                                    )
                                )
                                AccountStatesService.stateFlow(address).collectLatest { newMcBlockId ->
                                    val jettonData = async { getJettonData(liteApi, newMcBlockId, address) }
                                    val actualState = state.await()
                                    val newJettonData = jettonData.await()
                                    if (actualState.value != newJettonData) {
                                        actualState.update { newJettonData }
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }

    suspend fun jettonData(address: LiteServerAccountId?): JettonData? {
        if (address == null) return null
        return stateFlows[address]?.await()?.value
    }

    private suspend fun getJettonData(
        liteApi: LiteApi,
        mcBlockId: TonNodeBlockIdExt,
        address: LiteServerAccountId
    ): JettonData {
        val result = liteApi(
            LiteServerRunSmcMethod(
                0b100,
                mcBlockId,
                address,
                smcMethodId("get_jetton_data"),
                smcCreateParams(VmStack(VmStackList())).toByteArray()
            )
        ).result!!
        val stackValues = VmStack.loadTlb(BagOfCells(result).first()).toMutableVmStack()
        return JettonData(
            totalSupply = stackValues.popInt(),
            mintable = stackValues.popBool(),
            admin = MsgAddressInt.loadTlb(stackValues.popSlice()).toAccountId(),
            content = JettonContent.parse(stackValues.popCell()),
            walletCode = stackValues.popCell()
        ).also {
            log.info("new jetton data: $it")
        }
    }
}
