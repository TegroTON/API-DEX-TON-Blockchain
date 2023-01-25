package finance.tegro.core.entity

import finance.tegro.core.converter.MsgAddressConverter
import jakarta.persistence.*
import org.ton.block.MsgAddress
import java.math.BigInteger
import java.time.Instant
import java.util.*

@Entity(name = "token_supply")
@Table(name = "token_supplies")
open class TokenSupply(
    @Convert(converter = MsgAddressConverter::class)
    @Column(name = "address", nullable = false, columnDefinition = "BYTEA")
    open val address: MsgAddress,

    @Column(name = "supply", nullable = false, columnDefinition = "NUMERIC")
    open var supply: BigInteger,

    @ManyToOne(optional = false)
    @JoinColumn(name = "block_id", nullable = false)
    open var blockId: BlockId,

    @Column(name = "timestamp", nullable = false, columnDefinition = "TIMESTAMPTZ")
    open var timestamp: Instant? = Instant.now(),
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    open var id: UUID? = null

    @ManyToOne
    @JoinColumn(name = "token_id")
    open var token: Token? = null
}
