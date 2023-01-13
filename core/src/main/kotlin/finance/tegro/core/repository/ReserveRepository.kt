package finance.tegro.core.repository

import finance.tegro.core.entity.ExchangePair
import finance.tegro.core.entity.Reserve
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ReserveRepository : JpaRepository<Reserve, UUID> {
    fun findFirstByExchangePairOrderByBlockId_TimestampDesc(exchangePair: ExchangePair): Optional<Reserve>
}
