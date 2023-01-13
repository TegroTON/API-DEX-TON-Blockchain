package finance.tegro.core.repository;

import finance.tegro.core.entity.ExchangePairAdmin
import org.springframework.data.jpa.repository.JpaRepository
import org.ton.block.MsgAddress
import java.util.*

interface ExchangePairAdminRepository : JpaRepository<ExchangePairAdmin, UUID> {
    fun findByAddress(address: MsgAddress): Optional<ExchangePairAdmin>
}
