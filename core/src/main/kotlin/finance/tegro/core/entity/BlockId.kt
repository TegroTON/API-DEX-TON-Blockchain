package finance.tegro.core.entity

import jakarta.persistence.*
import org.ton.api.tonnode.TonNodeBlockId
import org.ton.api.tonnode.TonNodeBlockIdExt
import java.time.Instant
import java.util.*

@Entity(name = "block_id")
@Table(name = "block_ids", uniqueConstraints = [UniqueConstraint(columnNames = ["workchain", "shard", "seqno"])])
open class BlockId(
    @Column(name = "workchain", nullable = false)
    open override val workchain: Int,

    @Column(name = "shard", nullable = false)
    open override val shard: Long,

    @Column(name = "seqno", nullable = false)
    open override val seqno: Int,

    @Column(name = "root_hash", nullable = false, columnDefinition = "BYTEA")
    open val rootHash: ByteArray,

    @Column(name = "file_hash", nullable = false, columnDefinition = "BYTEA")
    open val fileHash: ByteArray,

    @Column(name = "timestamp", nullable = false, columnDefinition = "TIMESTAMPTZ")
    open var timestamp: Instant,
) : TonNodeBlockId {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    open var id: UUID? = null

    constructor(
        id: TonNodeBlockIdExt,
        instant: Instant,
    ) : this(
        id.workchain,
        id.shard,
        id.seqno,
        id.root_hash,
        id.file_hash,
        instant,
    )

    fun toTonNodeBlockIdExt(): TonNodeBlockIdExt = TonNodeBlockIdExt(workchain, shard, seqno, rootHash, fileHash)
}
