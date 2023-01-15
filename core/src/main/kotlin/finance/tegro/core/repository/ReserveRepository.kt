package finance.tegro.core.repository

import finance.tegro.core.entity.Reserve
import org.springframework.data.jpa.repository.JpaRepository
import org.ton.block.MsgAddress
import java.util.*

interface ReserveRepository : JpaRepository<Reserve, UUID> {
    fun findFirstByAddressOrderByBlockId_TimestampDesc(exchangePairAddress: MsgAddress): Optional<Reserve>
}
