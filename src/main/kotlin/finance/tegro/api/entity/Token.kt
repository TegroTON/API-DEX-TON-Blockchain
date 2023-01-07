package finance.tegro.api.entity

import finance.tegro.api.converter.MsgAddressConverter
import org.hibernate.annotations.NaturalId
import org.ton.block.MsgAddress
import java.time.Instant
import java.util.*
import javax.persistence.*

@Entity(name = "token")
@Table(name = "tokens")
open class Token(
    @NaturalId
    @Convert(converter = MsgAddressConverter::class)
    @Column(name = "address", nullable = false, unique = true, columnDefinition = "BYTEA")
    open val address: MsgAddress,

    @Column(name = "approved", nullable = false)
    open var approved: Boolean = false,

    @Column(name = "timestamp", nullable = false, columnDefinition = "TIMESTAMPTZ")
    open var timestamp: Instant = Instant.now()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    open var id: UUID? = null
}
