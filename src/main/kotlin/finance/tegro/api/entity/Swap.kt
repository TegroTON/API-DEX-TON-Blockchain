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

@Entity(name = "swap")
@Table(name = "swaps")
open class Swap(
    @Convert(converter = MsgAddressConverter::class)
    @Column(name = "source", nullable = false, columnDefinition = "BYTEA")
    open val source: MsgAddress,

    @Column(name = "amount_in", nullable = false, columnDefinition = "NUMERIC")
    open val amountIn: BigInteger,

    @Convert(converter = MsgAddressConverter::class)
    @Column(name = "destination", nullable = false, columnDefinition = "BYTEA")
    open val destination: MsgAddress,

    @Column(name = "amount_out", nullable = false, columnDefinition = "NUMERIC")
    open val amountOut: BigInteger,

    @Convert(converter = MsgAddressConverter::class)
    @Column(name = "referrer", nullable = true, columnDefinition = "BYTEA")
    open val referrer: MsgAddress?,

    @Convert(converter = TransactionConverter::class)
    @Column(name = "transaction", nullable = false, columnDefinition = "BYTEA")
    open val transaction: Transaction,

    @Convert(converter = TonNodeBlockIdExtConverter::class)
    @Column(name = "block", nullable = false, columnDefinition = "TEXT")
    open val block: TonNodeBlockIdExt,

    @Column(name = "timestamp", nullable = false, columnDefinition = "TIMESTAMPTZ")
    open val timestamp: Instant? = Instant.now(),
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    open var id: UUID? = null
}
