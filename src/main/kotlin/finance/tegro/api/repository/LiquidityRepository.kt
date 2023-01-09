package finance.tegro.api.repository

import finance.tegro.api.entity.Liquidity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.ton.block.MsgAddress
import java.util.*

@Repository
interface LiquidityRepository : JpaRepository<Liquidity, UUID> {
    fun findByAddress(address: MsgAddress): Optional<Liquidity>
    fun findByOwner(owner: MsgAddress): List<Liquidity>
    fun findByOwnerAndExchangePair(owner: MsgAddress, exchangePair: MsgAddress): Optional<Liquidity>
}