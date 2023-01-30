package finance.tegro.rest.v2.models

import io.ktor.util.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.bitstring.BitString
import org.ton.cell.Cell
import org.ton.contract.*
import org.ton.crypto.sha256

@Serializable
data class JettonContent(
    val uri: String? = null,
    val name: String? = null,
    val description: String? = null,
    val image: String? = null,
    @SerialName("image_data")
    val imageData: String? = null,
    val symbol: String? = null,
    val decimals: Int? = null
) {
    constructor(offchain: FullContent.OffChain) : this(uri = offchain.uri.data.flatten().decodeToString())
    constructor(entries: Map<BitString, ContentData>) : this(
        uri = entries[ONCHAIN_URI_KEY]?.flatten()?.decodeToString(),
        name = entries[ONCHAIN_NAME_KEY]?.flatten()?.decodeToString(),
        description = entries[ONCHAIN_DESCRIPTION_KEY]?.flatten()?.decodeToString(),
        image = entries[ONCHAIN_IMAGE_KEY]?.flatten()?.decodeToString(),
        imageData = entries[ONCHAIN_IMAGE_DATA_KEY]?.flatten()?.encodeBase64(),
        symbol = entries[ONCHAIN_SYMBOL_KEY]?.flatten()?.decodeToString(),
        decimals = entries[ONCHAIN_DECIMALS_KEY]?.flatten()?.decodeToString()?.toInt()
    )

    constructor(onchain: FullContent.OnChain) : this(onchain.data.toMap())

    companion object {
        private val ONCHAIN_URI_KEY = BitString(sha256("uri".toByteArray()))
        private val ONCHAIN_NAME_KEY = BitString(sha256("name".toByteArray()))
        private val ONCHAIN_DESCRIPTION_KEY = BitString(sha256("description".toByteArray()))
        private val ONCHAIN_IMAGE_KEY = BitString(sha256("image".toByteArray()))
        private val ONCHAIN_IMAGE_DATA_KEY = BitString(sha256("image_data".toByteArray()))
        private val ONCHAIN_SYMBOL_KEY = BitString(sha256("symbol".toByteArray()))
        private val ONCHAIN_DECIMALS_KEY = BitString(sha256("decimals".toByteArray()))

        fun parse(cell: Cell): JettonContent {
            return when (val fullContent = FullContent.loadTlb(cell)) {
                is FullContent.OnChain -> JettonContent(fullContent)
                is FullContent.OffChain -> JettonContent(fullContent)
            }
        }

        private fun SnakeData.flatten(): ByteArray = when (this) {
            is SnakeDataTail -> bits.toByteArray()
            is SnakeDataCons -> bits.toByteArray() + next.flatten()
        }

        private fun ContentData.flatten(): ByteArray = when (this) {
            is ContentData.Snake -> this.data.flatten()
            is ContentData.Chunks -> TODO("chunky content data")
        }
    }
}
