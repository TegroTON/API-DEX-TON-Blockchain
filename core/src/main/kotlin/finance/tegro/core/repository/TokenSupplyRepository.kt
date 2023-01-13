package finance.tegro.core.repository

import finance.tegro.core.entity.TokenSupply
import org.springframework.data.jpa.repository.JpaRepository
import org.ton.block.MsgAddress
import java.util.*

interface TokenSupplyRepository : JpaRepository<TokenSupply, UUID> {
    fun findFirstByAddressOrderByBlockId_TimestampDesc(address: MsgAddress): Optional<TokenSupply>
}
