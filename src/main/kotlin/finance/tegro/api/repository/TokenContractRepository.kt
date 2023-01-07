package finance.tegro.api.repository

import finance.tegro.api.entity.TokenContract
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.ton.block.MsgAddress
import java.util.*

@Repository
interface TokenContractRepository : JpaRepository<TokenContract, UUID> {
    fun findByAddress(address: MsgAddress): Optional<TokenContract>
    fun existsByAddress(address: MsgAddress): Boolean
}
