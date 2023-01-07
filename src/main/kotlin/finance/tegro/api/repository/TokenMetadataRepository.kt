package finance.tegro.api.repository

import finance.tegro.api.entity.TokenMetadata
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.ton.block.MsgAddress
import java.util.*

@Repository
interface TokenMetadataRepository : JpaRepository<TokenMetadata, UUID> {
    fun findByAddress(address: MsgAddress): Optional<TokenMetadata>
    fun existsByAddress(address: MsgAddress): Boolean
}
