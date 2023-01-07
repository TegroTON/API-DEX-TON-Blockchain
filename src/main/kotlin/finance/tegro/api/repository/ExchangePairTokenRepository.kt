package finance.tegro.api.repository

import finance.tegro.api.entity.ExchangePairToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.ton.block.MsgAddress
import java.util.*

@Repository
interface ExchangePairTokenRepository : JpaRepository<ExchangePairToken, UUID> {
    fun findByAddress(address: MsgAddress): Optional<ExchangePairToken>
}
