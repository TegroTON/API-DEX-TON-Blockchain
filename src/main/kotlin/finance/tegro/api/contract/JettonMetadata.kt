package finance.tegro.api.contract

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ByteArraySerializer
import mu.KLogging
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.ton.bitstring.BitString
import org.ton.cell.Cell
import org.ton.contract.*
import org.ton.crypto.sha256
import org.ton.tlb.loadTlb

@JsonIgnoreProperties(ignoreUnknown = true)
data class JettonMetadata(
    val uri: String?,
    val name: String?,
    val description: String?,
    val image: String?,
    @JsonSerialize(using = ByteArraySerializer::class)
    val imageData: ByteArray?,
    val symbol: String?,
    val decimals: Int = 9,
) {
    companion object : KLogging() {
        private val ONCHAIN_URI_KEY = BitString(sha256("uri".toByteArray()))
        private val ONCHAIN_NAME_KEY = BitString(sha256("name".toByteArray()))
        private val ONCHAIN_DESCRIPTION_KEY = BitString(sha256("description".toByteArray()))
        private val ONCHAIN_IMAGE_KEY = BitString(sha256("image".toByteArray()))
        private val ONCHAIN_IMAGE_DATA_KEY = BitString(sha256("image_data".toByteArray()))
        private val ONCHAIN_SYMBOL_KEY = BitString(sha256("symbol".toByteArray()))
        private val ONCHAIN_DECIMALS_KEY = BitString(sha256("decimals".toByteArray()))

        private suspend fun getOffchainMetadata(uri: String, webClient: WebClient): JettonMetadata =
            webClient.get()
                .uri(uri)
                .retrieve()
                .awaitBody()

        @JvmStatic
        suspend fun of(
            content: Cell,
            webClient: WebClient,
        ): JettonMetadata =
            when (val full = content.parse { loadTlb(FullContent) }) {
                is FullContent.OnChain -> {
                    val entries = full.data.toMap()

                    JettonMetadata(
                        uri = entries[ONCHAIN_URI_KEY]?.flatten()?.decodeToString(),
                        name = entries[ONCHAIN_NAME_KEY]?.flatten()?.decodeToString(),
                        description = entries[ONCHAIN_DESCRIPTION_KEY]?.flatten()?.decodeToString(),
                        image = entries[ONCHAIN_IMAGE_KEY]?.flatten()?.decodeToString(),
                        imageData = entries[ONCHAIN_IMAGE_DATA_KEY]?.flatten(),
                        symbol = entries[ONCHAIN_SYMBOL_KEY]?.flatten()?.decodeToString(),
                        decimals = entries[ONCHAIN_DECIMALS_KEY]?.flatten()?.decodeToString()?.toInt() ?: 9,
                    ).let {
                        if (it.uri != null) { // Semi- offchain metadata
                            val offchain = getOffchainMetadata(it.uri, webClient)

                            it.copy(
                                name = offchain.name ?: it.name,
                                description = offchain.description ?: it.description,
                                image = offchain.image ?: it.image,
                                imageData = offchain.imageData ?: it.imageData,
                                symbol = offchain.symbol ?: it.symbol,
                                decimals = offchain.decimals,
                            )
                        } else {
                            it
                        }
                    }
                }

                is FullContent.OffChain -> {
                    val uri = full.uri.data.flatten().decodeToString()
                    getOffchainMetadata(uri, webClient)
                        .copy(uri = uri)
                }
            }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JettonMetadata

        if (uri != other.uri) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (image != other.image) return false
        if (imageData != null) {
            if (other.imageData == null) return false
            if (!imageData.contentEquals(other.imageData)) return false
        } else if (other.imageData != null) return false
        if (symbol != other.symbol) return false
        if (decimals != other.decimals) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uri?.hashCode() ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (image?.hashCode() ?: 0)
        result = 31 * result + (imageData?.contentHashCode() ?: 0)
        result = 31 * result + (symbol?.hashCode() ?: 0)
        result = 31 * result + decimals
        return result
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
