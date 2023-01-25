package finance.tegro.core.entity

import finance.tegro.core.converter.MsgAddressConverter
import jakarta.persistence.*
import org.hibernate.annotations.NaturalId
import org.ton.block.MsgAddress
import java.time.Instant
import java.util.*

@Entity(name = "exchange_pair_admin")
@Table(name = "exchange_pair_admins")
open class ExchangePairAdmin(
    @NaturalId
    @Convert(converter = MsgAddressConverter::class)
    @Column(name = "address", nullable = false, unique = true, columnDefinition = "BYTEA")
    open val address: MsgAddress,

    @Convert(converter = MsgAddressConverter::class)
    @Column(name = "admin", nullable = false)
    open var admin: MsgAddress,

    @ManyToOne(optional = false)
    @JoinColumn(name = "block_id", nullable = false)
    open var blockId: BlockId,

    @Column(name = "timestamp", nullable = false, columnDefinition = "TIMESTAMPTZ")
    open var timestamp: Instant = Instant.now(),
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    open var id: UUID? = null
}
