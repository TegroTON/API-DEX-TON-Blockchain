package finance.tegro.rest.v2.services

import finance.tegro.rest.v2.dto.v1.ExchangePairDTOv1
import finance.tegro.rest.v2.dto.v1.ReserveDTOv1
import finance.tegro.rest.v2.dto.v1.TokenDTOv1
import finance.tegro.rest.v2.models.JettonContent
import finance.tegro.rest.v2.models.JettonData
import finance.tegro.rest.v2.models.Reserves
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.collections.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.slf4j.LoggerFactory
import org.ton.block.AddrStd
import org.ton.lite.api.liteserver.LiteServerAccountId
import java.math.BigInteger
import kotlin.coroutines.CoroutineContext

object PairV1CacheService : CoroutineScope {
    private val log = LoggerFactory.getLogger(PairV1CacheService::class.java)
    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineName(log.name)

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }
    private val uriCache = ConcurrentMap<String, JettonContent>()
    private val state = CompletableDeferred<MutableStateFlow<List<ExchangePairDTOv1>>>()
    private var job: Job? = null

    fun init() {
        job?.cancel()
        job = launch {
            while (true) {
                val list = PairsService.pairsAll().map { (liquidityAddress, baseAddress, quoteAddress) ->
                    async {
                        val timestamp = Clock.System.now()
                        val liquidity = async {
                            JettonDataService.jettonData(liquidityAddress).toTokenDtoV1(liquidityAddress, timestamp)
                        }
                        val base =
                            async { JettonDataService.jettonData(baseAddress).toTokenDtoV1(baseAddress, timestamp) }
                        val quote =
                            async { JettonDataService.jettonData(quoteAddress).toTokenDtoV1(quoteAddress, timestamp) }
                        val reserve =
                            ReservesService.reserves(liquidityAddress).value.toReserveDtoV1(liquidityAddress, timestamp)
                        ExchangePairDTOv1(
                            address = AddrStd(
                                liquidityAddress.workchain,
                                liquidityAddress.id
                            ).toString(userFriendly = true, urlSafe = true, bounceable = true),
                            timestamp = timestamp,
                            liquidity = liquidity.await(),
                            base = base.await(),
                            quote = quote.await(),
                            tokenTimestamp = timestamp,
                            reserve
                        )
                    }
                }.awaitAll().sortedByDescending {
                    it.reserve?.base
                }
                if (!state.isCompleted) {
                    state.complete(MutableStateFlow(list))
                } else {
                    val currentState = state.await()
                    currentState.update {
                        list
                    }
                }
                delay(5000)
            }
        }
    }

    suspend fun getPairs() = state.await().value

    private fun Reserves.toReserveDtoV1(
        address: LiteServerAccountId,
        timestamp: Instant = Clock.System.now()
    ): ReserveDTOv1 {
        return ReserveDTOv1(
            address = AddrStd(address.workchain, address.id).toString(
                userFriendly = true,
                urlSafe = true,
                bounceable = true
            ),
            base,
            quote,
            timestamp
        )
    }

    private suspend fun JettonData?.toTokenDtoV1(
        address: LiteServerAccountId?,
        timestamp: Instant = Clock.System.now()
    ): TokenDTOv1 {
        if (this == null) return TON_DATA
        if (address == null) return TON_DATA
        var name = content.name
        var symbol = content.symbol
        var description = content.description
        var decimals = content.decimals ?: 9
        if (this.content.uri != null) {
            val uri = if (content.uri.startsWith("ipfs://")) {
                "https://cloudflare-ipfs.com/ipfs/${content.uri.substring("ipfs://".length)}"
            } else content.uri
            val jettonContent = uriCache[uri] ?: try {
                httpClient.get(uri).body<JettonContent>().also {
                    uriCache[uri] = it
                }
            } catch (e: Exception) {
                log.error("Failed get content by uri: $uri", e)
                null
            }
            name = jettonContent?.name ?: name
            symbol = jettonContent?.symbol ?: symbol
            description = jettonContent?.description ?: description
            decimals = jettonContent?.decimals ?: decimals
        }
        return TokenDTOv1(
            address = AddrStd(address.workchain, address.id).toString(
                userFriendly = true,
                urlSafe = true,
                bounceable = true
            ),
            timestamp = timestamp,
            totalSupply = totalSupply,
            mintable = mintable,
            admin = admin?.let { AddrStd(it.workchain, it.id) }
                ?.toString(userFriendly = true, urlSafe = true, bounceable = true),
            contractTimestamp = timestamp,
            name = name,
            description = description,
            symbol = symbol,
            decimals = decimals,
            metadataTimestamp = timestamp
        )
    }

    private val TON_DATA = TokenDTOv1(
        address = null,
        timestamp = Clock.System.now(),
        totalSupply = BigInteger("5000000000000000000"),
        mintable = false,
        admin = null,
        contractTimestamp = Clock.System.now(),
        name = "Toncoin",
        description = "The native currency of the TON blockchain",
        symbol = "TON",
        decimals = 9,
        metadataTimestamp = Clock.System.now()
    )
}
