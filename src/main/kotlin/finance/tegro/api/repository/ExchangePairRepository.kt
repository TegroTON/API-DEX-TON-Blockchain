package finance.tegro.api.repository

import finance.tegro.api.entity.ExchangePair
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.ton.block.MsgAddress
import java.util.*

@Repository
interface ExchangePairRepository : JpaRepository<ExchangePair, UUID> {
    fun findByAddress(address: MsgAddress): Optional<ExchangePair>

    fun existsByAddress(address: MsgAddress): Boolean
}
