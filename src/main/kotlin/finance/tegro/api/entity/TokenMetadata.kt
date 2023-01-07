package finance.tegro.api.entity

import finance.tegro.api.converter.MsgAddressConverter
import finance.tegro.api.converter.TonNodeBlockIdExtConverter
import org.hibernate.annotations.NaturalId
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.block.MsgAddress
import java.time.Instant
import java.util.*
import javax.persistence.*

@Entity(name = "token_metadata")
@Table
open class TokenMetadata(
    @NaturalId
    @Convert(converter = MsgAddressConverter::class)
    @Column(name = "address", nullable = false, unique = true, columnDefinition = "BYTEA")
    open val address: MsgAddress,

    @Column(name = "uri", columnDefinition = "TEXT")
    open var uri: String? = null,

    @Column(name = "name", columnDefinition = "TEXT")
    open var name: String? = null,

    @Column(name = "description", columnDefinition = "TEXT")
    open var description: String? = null,

    @Column(name = "image", columnDefinition = "TEXT")
    open var image: String? = null,

    @Column(name = "image_data", columnDefinition = "BYTEA")
    open var imageData: ByteArray? = null,

    @Column(name = "symbol", columnDefinition = "TEXT")
    open var symbol: String? = null,

    @Column(name = "decimals", nullable = false)
    open var decimals: Int = 9,

    @Convert(converter = TonNodeBlockIdExtConverter::class)
    @Column(name = "block", nullable = false, columnDefinition = "TEXT")
    open var block: TonNodeBlockIdExt,

    @Column(name = "timestamp", nullable = false, columnDefinition = "TIMESTAMPTZ")
    open var timestamp: Instant? = Instant.now(),
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    open var id: UUID? = null
}
