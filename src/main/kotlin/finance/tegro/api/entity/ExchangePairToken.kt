package finance.tegro.api.entity

import finance.tegro.api.converter.MsgAddressConverter
import finance.tegro.api.converter.TonNodeBlockIdExtConverter
import org.hibernate.annotations.NaturalId
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.block.MsgAddress
import java.time.Instant
import java.util.*
import javax.persistence.*

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

    @Convert(converter = TonNodeBlockIdExtConverter::class)
    @Column(name = "block", nullable = false, columnDefinition = "TEXT")
    open var block: TonNodeBlockIdExt,

    @Column(name = "timestamp", nullable = false, columnDefinition = "TIMESTAMPTZ")
    open var timestamp: Instant = Instant.now()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    open var id: UUID? = null
}
