package finance.tegro.core.entity

import finance.tegro.core.converter.MsgAddressConverter
import jakarta.persistence.*
import org.hibernate.annotations.NaturalId
import org.ton.block.MsgAddress
import java.time.Instant
import java.util.*

@Entity(name = "exchange_pair_token")
@Table(
    name = "exchange_pair_tokens", uniqueConstraints = [
        UniqueConstraint(columnNames = ["base", "quote"])
    ]
)
open class ExchangePairToken(
    @NaturalId
    @Convert(converter = MsgAddressConverter::class)
    @Column(name = "address", nullable = false, unique = true, columnDefinition = "BYTEA")
    open val address: MsgAddress,

    @Convert(converter = MsgAddressConverter::class)
    @Column(name = "base", nullable = false, columnDefinition = "BYTEA")
    open var base: MsgAddress,

    @Convert(converter = MsgAddressConverter::class)
    @Column(name = "quote", nullable = false, columnDefinition = "BYTEA")
    open var quote: MsgAddress,

    @ManyToOne(optional = false)
    @JoinColumn(name = "block_id", nullable = false)
    open var blockId: BlockId,

    @Column(name = "timestamp", nullable = false, columnDefinition = "TIMESTAMPTZ")
    open var timestamp: Instant = Instant.now()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    open var id: UUID? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liquidity_token_id")
    open var liquidityToken: Token? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_token_id")
    open var baseToken: Token? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_token_id")
    open var quoteToken: Token? = null
}
