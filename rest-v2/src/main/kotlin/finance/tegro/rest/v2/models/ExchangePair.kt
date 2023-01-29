@file:UseSerializers(RawAccountIdSerializer::class)

package finance.tegro.rest.v2.models

import finance.tegro.rest.v2.utils.RawAccountIdSerializer
import finance.tegro.rest.v2.utils.toAccountId
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.UseSerializers
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.selectAll
import org.ton.block.MsgAddress
import org.ton.boc.BagOfCells
import org.ton.lite.api.liteserver.LiteServerAccountId
import kotlin.time.Duration.Companion.minutes

object ExchangePairs : UUIDTable("exchange_pairs") {
    val address = binary("address")
    val timestamp = timestamp("timestamp")
    val adminId = uuid("admin_id")
    val tokenId = uuid("token_id")
}

interface ExchangePairFacade {
    suspend fun allExchangePairs(): List<LiteServerAccountId>

    companion object {
        fun init(): ExchangePairFacade = ExchangePairFacadeImpl()
        fun cached(delegate: ExchangePairFacade = init()): ExchangePairFacade = ExchangePairFacadeCacheImpl(delegate)
    }
}

private class ExchangePairFacadeImpl : ExchangePairFacade {
    private fun resultRowToExchangePair(row: ResultRow) =
        MsgAddress.loadTlb(BagOfCells(row[ExchangePairs.address]).roots.first()).toAccountId()

    override suspend fun allExchangePairs(): List<LiteServerAccountId> = dbQuery {
        ExchangePairs.selectAll().mapNotNull(::resultRowToExchangePair)
    }
}

private class ExchangePairFacadeCacheImpl(
    private val delegate: ExchangePairFacade
) : ExchangePairFacade {
    val allExchangePairsLastUpdate = atomic(Instant.DISTANT_PAST)
    val allExchangePairs = atomic<Deferred<List<LiteServerAccountId>>?>(null)

    override suspend fun allExchangePairs(): List<LiteServerAccountId> = coroutineScope {
        val now = Clock.System.now()
        val currentAllExchangePairs = allExchangePairs.value
        if (currentAllExchangePairs == null || now - allExchangePairsLastUpdate.value > 1.minutes) {
            val job = async {
                delegate.allExchangePairs()
            }
            allExchangePairs.value = job
            allExchangePairsLastUpdate.value = now
            job.await()
        } else {
            currentAllExchangePairs.await()
        }
    }
}
