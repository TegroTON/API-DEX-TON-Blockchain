package finance.tegro.core.repository

import finance.tegro.core.entity.TokenContract
import org.springframework.data.jpa.repository.JpaRepository
import org.ton.block.MsgAddress
import java.util.*

interface TokenContractRepository : JpaRepository<TokenContract, UUID> {
    fun findByAddress(address: MsgAddress): Optional<TokenContract>
}
