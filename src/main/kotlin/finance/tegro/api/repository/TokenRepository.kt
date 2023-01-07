package finance.tegro.api.repository

import finance.tegro.api.entity.Token
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.ton.block.MsgAddress
import java.util.*

@Repository
interface TokenRepository : JpaRepository<Token, UUID> {
    fun findByAddress(address: MsgAddress): Optional<Token>
    fun existsByAddress(address: MsgAddress): Boolean
}
