package finance.tegro.core.repository;

import finance.tegro.core.converter.MsgAddressConverter
import finance.tegro.core.entity.ExchangePair
import finance.tegro.core.entity.Swap
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.ton.block.MsgAddress
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.persistence.EntityManager

interface SwapRepository : JpaRepository<Swap, UUID> {
    fun findByExchangePair(exchangePair: ExchangePair, pageable: Pageable): Page<Swap>

    @Query(
        """
        SELECT TRUNC(((s.quote_amount / (10.0 ^ tmq.decimals)) / (s.base_amount / (10.0 ^ tmb.decimals))), tmq.decimals)
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

    fun findFirstByDestinationOrderByBlockId_TimestampAsc(destination: MsgAddress): Optional<Swap>

    @Query(
        """
        SELECT DISTINCT s.destination 
        FROM swaps s
            JOIN block_ids bi on s.block_id = bi.id
        WHERE s.referrer = ?1
            AND NOT EXISTS (
                SELECT *
                FROM exchange_pairs ep
                WHERE ep.address = s.destination -- exclude routed swaps
            )
            AND NOT EXISTS (
                SELECT s2
                FROM swaps s2
                    JOIN block_ids bi2 on s2.block_id = bi2.id
                WHERE s.destination = s2.destination -- Same destination
                    AND bi2.timestamp < bi.timestamp -- Before referral
            )
    """,
        nativeQuery = true
    )
    fun findAllReferrals(referrer: MsgAddress): List<MsgAddress>

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

    companion object {
        @JvmStatic
        fun findAllReferrals(entityManager: EntityManager, referrer: MsgAddress): List<MsgAddress> =
            entityManager.createNativeQuery(
                """
        SELECT DISTINCT s.destination 
        FROM swaps s
            JOIN block_ids bi on s.block_id = bi.id
        WHERE s.referrer = ?1
            AND NOT EXISTS (
                SELECT *
                FROM exchange_pairs ep
                WHERE ep.address = s.destination -- exclude routed swaps
            )
            AND NOT EXISTS (
                SELECT s2
                FROM swaps s2
                    JOIN block_ids bi2 on s2.block_id = bi2.id
                WHERE s.destination = s2.destination -- Same destination
                    AND bi2.timestamp < bi.timestamp -- Before referral
            )
    """
            ).setParameter(1, MsgAddressConverter().convertToDatabaseColumn(referrer))
                .resultList
                .map { MsgAddressConverter().convertToEntityAttribute(it as ByteArray) }

        @JvmStatic
        fun findAccountVolume(entityManager: EntityManager, account: MsgAddress): Map<Instant, BigInteger> =
            entityManager.createNativeQuery(
                """
        SELECT bi.timestamp, s.base_amount
        FROM swaps s
                 JOIN block_ids bi on s.block_id = bi.id
                 JOIN exchange_pairs ep on s.exchange_pair_id = ep.id
                 JOIN exchange_pair_tokens ept on ep.token_id = ept.id
        WHERE s.destination = ?1 AND ept.base = '\xB5EE9C7201010101000300000120' -- Just TON
        """
            ).setParameter(1, MsgAddressConverter().convertToDatabaseColumn(account))
                .resultList
                .map { it as Array<*> }
                .associate {
                    (it[0] as Timestamp).toInstant() to (it[1] as BigDecimal).toBigInteger()
                }
    }
}
