package finance.tegro.core.entity

import finance.tegro.core.converter.CellConverter
import finance.tegro.core.converter.MsgAddressConverter
import jakarta.persistence.*
import org.hibernate.annotations.NaturalId
import org.ton.block.MsgAddress
import org.ton.cell.Cell
import java.math.BigInteger
import java.time.Instant
import java.util.*

@Entity(name = "token_contract")
@Table(name = "token_contracts")
open class TokenContract(
    @NaturalId
    @Convert(converter = MsgAddressConverter::class)
    @Column(name = "address", nullable = false, unique = true, columnDefinition = "BYTEA")
    open val address: MsgAddress,

    @Column(name = "total_supply", nullable = false, columnDefinition = "NUMERIC")
    open var totalSupply: BigInteger,

    @Column(name = "mintable", nullable = false)
    open var mintable: Boolean,

    @Convert(converter = MsgAddressConverter::class)
    @Column(name = "admin", nullable = false, columnDefinition = "BYTEA")
    open var admin: MsgAddress,

    @Convert(converter = CellConverter::class)
    @Column(name = "content", nullable = false, columnDefinition = "BYTEA")
    open var content: Cell,

    @Convert(converter = CellConverter::class)
    @Column(name = "wallet_code", nullable = false, columnDefinition = "BYTEA")
    open var walletCode: Cell,

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
}
