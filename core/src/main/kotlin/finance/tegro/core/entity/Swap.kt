package finance.tegro.core.entity

import finance.tegro.core.converter.MsgAddressConverter
import finance.tegro.core.converter.TransactionConverter
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
    @Column(name = "destination", nullable = false, columnDefinition = "BYTEA")
    open val destination: MsgAddress,

    @Column(name = "base_amount", nullable = false, columnDefinition = "NUMERIC")
    open val baseAmount: BigInteger,

    @Convert(converter = MsgAddressConverter::class)
    @Column(name = "exchange_pair_address", nullable = false, columnDefinition = "BYTEA")
    open val exchangePairAddress: MsgAddress,

    @Column(name = "quote_amount", nullable = false, columnDefinition = "NUMERIC")
    open val quoteAmount: BigInteger,

    @Column(name = "inverse", nullable = false)
    open val inverse: Boolean,

    @Convert(converter = MsgAddressConverter::class)
    @Column(name = "referrer", nullable = false, columnDefinition = "BYTEA")
    open val referrer: MsgAddress,

    @Column(name = "query_id", nullable = false)
    open val queryId: ULong,

    @Convert(converter = TransactionConverter::class)
    @Column(name = "transaction", nullable = false, columnDefinition = "BYTEA")
    open val transaction: Transaction,

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
