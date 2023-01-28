package finance.tegro.rest.v2.services

import finance.tegro.rest.v2.dto.v1.ExchangePairDTOv1
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
import kotlin.coroutines.CoroutineContext

object PairV1CacheService : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineName("PairV1CacheService")

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
                    state.update { newPairs }
                    println("new pairs: $newPairs")
                }
                delay(1000)
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
