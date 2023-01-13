package finance.tegro.core.repository

import finance.tegro.core.entity.Liquidity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.ton.block.MsgAddress
import java.util.*

interface LiquidityRepository : JpaRepository<Liquidity, UUID> {
    fun findFirstByOwnerAndExchangePairAddressOrderByBlockId_TimestampDesc(
        owner: MsgAddress,
        exchangePairAddress: MsgAddress
    ): Optional<Liquidity>

    @Query("select distinct l.exchangePairAddress from liquidity l where l.owner = ?1")
    fun findDistinctExchangePairAddressByOwner(owner: MsgAddress): List<MsgAddress>
}
