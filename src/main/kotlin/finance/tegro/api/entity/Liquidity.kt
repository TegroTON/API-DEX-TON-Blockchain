package finance.tegro.api.entity

import finance.tegro.api.converter.MsgAddressConverter
import finance.tegro.api.converter.TonNodeBlockIdExtConverter
import finance.tegro.api.converter.TransactionConverter
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.block.MsgAddress
import org.ton.block.Transaction
import java.math.BigInteger
import java.time.Instant
import java.util.*
import javax.persistence.*

@Entity(name = "liquidity")
@Table
open class Liquidity(
    @Convert(converter = MsgAddressConverter::class)
    @Column(name = "address", nullable = false, columnDefinition = "BYTEA")
    open val address: MsgAddress,

    @Convert(converter = MsgAddressConverter::class)
    @Column(name = "exchange_pair", nullable = false, columnDefinition = "BYTEA")
    open val exchangePair: MsgAddress,

    @Column(name = "amount", nullable = false, columnDefinition = "NUMERIC")
    open val amount: BigInteger,

    @Column(name = "burned", nullable = false)
    open val burned: Boolean,

    @Convert(converter = TransactionConverter::class)
    @Column(name = "transaction", nullable = false, columnDefinition = "BYTEA")
    open val transaction: Transaction,

    @Convert(converter = TonNodeBlockIdExtConverter::class)
    @Column(name = "block", nullable = false, columnDefinition = "TEXT")
    open val block: TonNodeBlockIdExt,

    @Column(name = "timestamp", nullable = false, columnDefinition = "TIMESTAMPTZ")
    open val timestamp: Instant = Instant.now(),
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    open var id: UUID? = null
}
