package finance.tegro.core.repository;

import finance.tegro.core.entity.ExchangePairToken
import finance.tegro.core.entity.Token
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import org.ton.block.MsgAddress
import java.util.*

interface ExchangePairTokenRepository : JpaRepository<ExchangePairToken, UUID> {
    fun findByAddress(address: MsgAddress): Optional<ExchangePairToken>
    fun findByBaseAndQuote(base: MsgAddress, quote: MsgAddress): Optional<ExchangePairToken>

    fun findByBase(base: MsgAddress): List<ExchangePairToken>
    fun findByQuote(quote: MsgAddress): List<ExchangePairToken>

    @Transactional
    @Modifying
    @Query("update exchange_pair_token e set e.liquidityToken = ?1, e.timestamp = now() where e.address = ?2")
    fun updateLiquidityTokenByAddress(liquidityToken: Token, address: MsgAddress)

    @Transactional
    @Modifying
    @Query("update exchange_pair_token e set e.baseToken = ?1, e.timestamp = now() where e.base = ?2")
    fun updateBaseTokenByBase(baseToken: Token, base: MsgAddress)


    @Transactional
    @Modifying
    @Query("update exchange_pair_token e set e.quoteToken = ?1, e.timestamp = now() where e.quote = ?2")
    fun updateQuoteTokenByQuote(quoteToken: Token, quote: MsgAddress)
}
