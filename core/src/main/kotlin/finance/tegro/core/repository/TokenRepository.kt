package finance.tegro.core.repository

import finance.tegro.core.entity.Token
import finance.tegro.core.entity.TokenContract
import finance.tegro.core.entity.TokenMetadata
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import org.ton.block.MsgAddress
import java.util.*

interface TokenRepository : JpaRepository<Token, UUID> {
    fun existsByAddress(address: MsgAddress): Boolean
    fun findByAddress(address: MsgAddress): Optional<Token>

    @Transactional
    @Modifying
    @Query("update token t set t.contract = ?1, t.timestamp = now() where t.address = ?2")
    fun updateContractByAddress(contract: TokenContract, address: MsgAddress)

    @Transactional
    @Modifying
    @Query("update token t set t.metadata = ?1, t.timestamp = now() where t.address = ?2")
    fun updateMetadataByAddress(metadata: TokenMetadata, address: MsgAddress)

    @Query(
        """
        SELECT *
        FROM tokens t
        WHERE NOT EXISTS (
            SELECT DISTINCT e.liquidity_token_id
            FROM exchange_pair_tokens e
            WHERE e.liquidity_token_id = t.id
        )
    """,
        nativeQuery = true
    )
    fun findAllNonLiquidityTokens(): List<Token>
}
