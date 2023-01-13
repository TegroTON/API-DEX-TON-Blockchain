package finance.tegro.core.repository

import finance.tegro.core.entity.TokenMetadata
import org.springframework.data.jpa.repository.JpaRepository
import org.ton.block.MsgAddress
import java.util.*

interface TokenMetadataRepository : JpaRepository<TokenMetadata, UUID> {
    fun findByAddress(address: MsgAddress): Optional<TokenMetadata>
}
