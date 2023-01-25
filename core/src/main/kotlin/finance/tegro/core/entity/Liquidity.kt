package finance.tegro.core.entity

import finance.tegro.core.converter.MsgAddressConverter
import jakarta.persistence.*
import org.ton.block.MsgAddress
import java.math.BigInteger
import java.time.Instant
import java.util.*

@Entity(name = "liquidity")
@Table
open class Liquidity(
    @Convert(converter = MsgAddressConverter::class)
    @Column(name = "address", nullable = false, columnDefinition = "BYTEA")
    open val address: MsgAddress,

    @Convert(converter = MsgAddressConverter::class)
    @Column(name = "owner", nullable = false, columnDefinition = "BYTEA")
    open val owner: MsgAddress,

    @Convert(converter = MsgAddressConverter::class)
    @Column(name = "exchange_pair_address", nullable = false, columnDefinition = "BYTEA")
    open val exchangePairAddress: MsgAddress,

    @Column(name = "balance", nullable = false, columnDefinition = "NUMERIC")
    open val balance: BigInteger,

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
