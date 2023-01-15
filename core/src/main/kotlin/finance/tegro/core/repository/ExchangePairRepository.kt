package finance.tegro.core.repository

import finance.tegro.core.entity.ExchangePair
import finance.tegro.core.entity.ExchangePairAdmin
import finance.tegro.core.entity.ExchangePairToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import org.ton.block.MsgAddress
import java.util.*

interface ExchangePairRepository : JpaRepository<ExchangePair, UUID> {
    fun findByAddress(address: MsgAddress): Optional<ExchangePair>
    fun existsByAddress(address: MsgAddress): Boolean

    @Transactional
    @Modifying
    @Query("update exchange_pair e set e.admin = ?1, e.timestamp = now() where e.address = ?2")
    fun updateAdminByAddress(admin: ExchangePairAdmin, address: MsgAddress)

    @Transactional
    @Modifying
    @Query("update exchange_pair e set e.token = ?1, e.timestamp = now() where e.address = ?2")
    fun updateTokenByAddress(token: ExchangePairToken, address: MsgAddress)

    @Query(
        """
            select e from exchange_pair e
            where upper(e.token.baseToken.metadata.symbol) = upper(?1)
             and upper(e.token.quoteToken.metadata.symbol) = upper(?2)
         """
    )
    fun findByBaseAndQuoteTokenSymbols(
        base: String,
        quote: String
    ): Optional<ExchangePair>

}
