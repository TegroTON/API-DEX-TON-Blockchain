package finance.tegro.api.entity

import finance.tegro.api.converter.MsgAddressConverter
import finance.tegro.api.converter.TonNodeBlockIdExtConverter
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.block.MsgAddress
import java.math.BigInteger
import java.time.Instant
import java.util.*
import javax.persistence.*

@Entity(name = "liquidity")
@Table(
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["owner", "exchange_pair"])
    ]
)
open class Liquidity(
    @Convert(converter = MsgAddressConverter::class)
    @Column(name = "address", nullable = false, unique = true, columnDefinition = "BYTEA")
    open val address: MsgAddress,

    @Convert(converter = MsgAddressConverter::class)
    @Column(name = "owner", nullable = false, columnDefinition = "BYTEA")
    open var owner: MsgAddress,

    @Convert(converter = MsgAddressConverter::class)
    @Column(name = "exchange_pair", nullable = false, columnDefinition = "BYTEA")
    open var exchangePair: MsgAddress,

    @Column(name = "balance", nullable = false, columnDefinition = "NUMERIC")
    open var balance: BigInteger,

    @Convert(converter = TonNodeBlockIdExtConverter::class)
    @Column(name = "block", nullable = false, columnDefinition = "TEXT")
    open var block: TonNodeBlockIdExt,

    @Column(name = "timestamp", nullable = false, columnDefinition = "TIMESTAMPTZ")
    open var timestamp: Instant = Instant.now(),
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    open var id: UUID? = null
}
