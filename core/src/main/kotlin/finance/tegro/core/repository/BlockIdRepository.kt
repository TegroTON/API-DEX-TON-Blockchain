package finance.tegro.core.repository

import finance.tegro.core.entity.BlockId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

interface BlockIdRepository : JpaRepository<BlockId, UUID> {
    fun existsByWorkchainAndShardAndSeqno(workchain: Int, shard: Long, seqno: Int): Boolean

    fun findTopByWorkchainOrderBySeqno(workchain: Int): Optional<BlockId>

    @Transactional
    @Modifying
    @Query("update block_id b set b.timestamp = ?1 where b.workchain = ?2 and b.shard = ?3 and b.seqno = ?4")
    fun updateTimestampByWorkchainAndShardAndSeqno(timestamp: Instant, workchain: Int, shard: Long, seqno: Int)

    @Query(
        """
            SELECT s.i
            FROM generate_series(?2, (SELECT MAX(seqno) FROM block_ids WHERE workchain = ?1)) s(i)
            WHERE NOT EXISTS (SELECT 1 from block_ids where seqno = s.i and workchain = ?1)
            LIMIT ?3
        """,
        nativeQuery = true
    )
    fun findMissingSeqnos(workchain: Int, startSeqno: Int, limit: Int): List<Int>
}
