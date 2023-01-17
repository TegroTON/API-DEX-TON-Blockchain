package finance.tegro.core.repository;

import finance.tegro.core.entity.ExchangePair
import finance.tegro.core.entity.Swap
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.ton.block.MsgAddress
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

interface SwapRepository : JpaRepository<Swap, UUID> {
    fun findByExchangePair(exchangePair: ExchangePair, pageable: Pageable): Page<Swap>

    @Query(
        """
        SELECT TRUNC(((s.base_amount / (10.0 ^ tmb.decimals)) / (s.quote_amount / (10.0 ^ tmq.decimals))), tmb.decimals)
        FROM swaps s
                 JOIN block_ids bi on s.block_id = bi.id
                 JOIN exchange_pairs ep on s.exchange_pair_id = ep.id
                 JOIN exchange_pair_tokens ept on ep.token_id = ept.id
                 JOIN tokens tb on ept.base_token_id = tb.id
                 JOIN token_metadata tmb on tb.metadata_id = tmb.id
                 JOIN tokens tq on ept.quote_token_id = tq.id
                 JOIN token_metadata tmq on tq.metadata_id = tmq.id
        WHERE s.base_amount > 0 -- base_amount can be negative in some cases
             AND s.exchange_pair_id = ?1
             AND bi.timestamp <= ?2
        ORDER BY bi.timestamp DESC
        LIMIT 1
    """,
        nativeQuery = true
    )
    fun findPriceOn(
        exchangePair: ExchangePair,
        timestamp: Instant = Instant.now().minus(24, ChronoUnit.HOURS)
    ): Optional<BigDecimal>

    @Query(
        """
        SELECT SUM(TRUNC(s.base_amount / (10.0 ^ tmb.decimals), tmb.decimals))
        FROM swaps s
                 JOIN block_ids bi on s.block_id = bi.id
                 JOIN exchange_pairs ep on s.exchange_pair_id = ep.id
                 JOIN exchange_pair_tokens ept on ep.token_id = ept.id
                 JOIN tokens tb on ept.base_token_id = tb.id
                 JOIN token_metadata tmb on tb.metadata_id = tmb.id
        WHERE s.exchange_pair_id = ?1
             AND bi.timestamp > ?2
        """,
        nativeQuery = true
    )
    fun findBaseVolume(
        exchangePair: ExchangePair,
        startTime: Instant = Instant.now().minus(24, ChronoUnit.HOURS)
    ): Optional<BigDecimal>

    @Query(
        """
        SELECT DISTINCT s.destination 
        FROM swap s
            JOIN exchange_pair AS ep ON ep.address = s.destination
        WHERE ep.address IS NULL AND s.referrer = ?1
    """
    )
    fun findAllReferrals(referrer: MsgAddress): List<MsgAddress>

    @Query(
        """
        SELECT s
        FROM swap s
            JOIN block_id bi on s.blockId = bi.id
        WHERE s.referrer = ?1 AND s.destination = ?2
        ORDER BY bi.timestamp ASC
    """
    )
    fun findFirstReferralSwap(referrer: MsgAddress, referral: MsgAddress): Optional<Swap>


    @Query(
        """
        SELECT SUM(s.base_amount)
        FROM swaps s
                 JOIN block_ids bi on s.block_id = bi.id
                 JOIN exchange_pairs ep on s.exchange_pair_id = ep.id
                 JOIN exchange_pair_tokens ept on ep.token_id = ept.id
        WHERE s.destination = ?1 AND ept.base = '\xB5EE9C7201010101000300000120' -- Just TON
                AND bi.timestamp > ?2
        """,
        nativeQuery = true
    )
    fun findAccountVolumeTON(account: MsgAddress, startTime: Instant): Optional<BigInteger>


    @Query(
        """
        SELECT SUM(TRUNC(s.quote_amount / (10.0 ^ tmq.decimals), tmq.decimals))
        FROM swaps s
                 JOIN block_ids bi on s.block_id = bi.id
                 JOIN exchange_pairs ep on s.exchange_pair_id = ep.id
                 JOIN exchange_pair_tokens ept on ep.token_id = ept.id
                 JOIN tokens tq on ept.quote_token_id = tq.id
                 JOIN token_metadata tmq on tq.metadata_id = tmq.id
        WHERE s.exchange_pair_id = ?1
             AND bi.timestamp > ?2
        """,
        nativeQuery = true
    )
    fun findQuoteVolume(
        exchangePair: ExchangePair,
        startTime: Instant = Instant.now().minus(24, ChronoUnit.HOURS)
    ): Optional<BigDecimal>
}
