package finance.tegro.core.entity

import finance.tegro.core.converter.MsgAddressConverter
import org.ton.block.MsgAddress
import java.math.BigInteger
import java.time.Instant
import java.util.*
import javax.persistence.*

@Entity(name = "reserve")
@Table(name = "reserves")
open class Reserve(
    @Convert(converter = MsgAddressConverter::class)
    @Column(name = "address", nullable = false, columnDefinition = "BYTEA")
    open val address: MsgAddress,

    @Column(name = "base", nullable = false, columnDefinition = "NUMERIC")
    open val base: BigInteger,

    @Column(name = "quote", nullable = false, columnDefinition = "NUMERIC")
    open val quote: BigInteger,

    @ManyToOne(optional = false)
    @JoinColumn(name = "block_id", nullable = false)
    open var blockId: BlockId,

    @Column(name = "timestamp", nullable = false, columnDefinition = "TIMESTAMPTZ")
    open val timestamp: Instant = Instant.now(),
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    open var id: UUID? = null

    @ManyToOne
    @JoinColumn(name = "exchange_pair_id")
    open var exchangePair: ExchangePair? = null
}
