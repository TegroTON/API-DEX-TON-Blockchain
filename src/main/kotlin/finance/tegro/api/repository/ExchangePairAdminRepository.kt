package finance.tegro.api.repository

import finance.tegro.api.entity.ExchangePairAdmin
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.ton.block.MsgAddress
import java.util.*

@Repository
interface ExchangePairAdminRepository : JpaRepository<ExchangePairAdmin, UUID> {
    fun findByAddress(address: MsgAddress): Optional<ExchangePairAdmin>
}
