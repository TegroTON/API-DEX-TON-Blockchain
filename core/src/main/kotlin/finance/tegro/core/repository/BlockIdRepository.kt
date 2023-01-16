package finance.tegro.core.repository

import finance.tegro.core.entity.BlockId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface BlockIdRepository : JpaRepository<BlockId, UUID> {
    fun existsByWorkchainAndSeqno(workchain: Int, seqno: Int): Boolean
    fun existsByWorkchainAndShardAndSeqno(workchain: Int, shard: Long, seqno: Int): Boolean

    fun findFirstByWorkchainOrderByTimestampDesc(workchain: Int): Optional<BlockId>

    fun findFirstByWorkchainOrderBySeqnoAsc(workchain: Int): Optional<BlockId>

    @Query(
        """
            SELECT s.i
            FROM generate_series(?2, (SELECT MAX(seqno) FROM block_ids WHERE workchain = ?1)) s(i)
            WHERE NOT EXISTS (SELECT 1 from block_ids where seqno = s.i and workchain = ?1)
            ORDER BY s.i DESC
            LIMIT ?3
        """,
        nativeQuery = true
    )
    fun findMissingSeqnos(workchain: Int, startSeqno: Int, limit: Int): List<Int>
}
