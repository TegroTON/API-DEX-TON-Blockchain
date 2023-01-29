package finance.tegro.rest.v2.services

import finance.tegro.rest.v2.dto.v1.ExchangePairDTOv1
import finance.tegro.rest.v2.dto.v1.ReserveDTOv1
import finance.tegro.rest.v2.utils.toAccountId
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory
import org.ton.block.AddrStd
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.minutes

object PairV1CacheService : CoroutineScope {
    private val log = LoggerFactory.getLogger(PairV1CacheService::class.java)
    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineName(log.name)

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }
    private val state: MutableStateFlow<List<ExchangePairDTOv1>?> = MutableStateFlow(null)
    private val job = launch {
        while (true) {
            try {
                val newPairs = getPairsV1()
                if (newPairs != null && state.value != newPairs) {
                    state.update {
                        newPairs.mapNotNull {
                            val accountId =
                                AddrStd.parseUserFriendly(it.address).toAccountId() ?: return@mapNotNull null
                            val reserves = ReservesService.reserves(accountId).value
                            it.copy(
                                reserve = ReserveDTOv1(
                                    address = it.address,
                                    base = reserves.base,
                                    quote = reserves.quote,
                                    timestamp = Clock.System.now()
                                )
                            )
                        }
                    }
                    log.info("new cached pairs: $newPairs")
                }
                delay(5.minutes)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                delay(5000)
            }
        }
    }

    fun getPairs() = state.value

    private suspend fun getPairsV1(): List<ExchangePairDTOv1>? {
        return httpClient.get("https://api.tegro.finance/v1/pair").body()
    }
}
