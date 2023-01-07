package finance.tegro.api.processor

import finance.tegro.api.contract.JettonMetadata
import finance.tegro.api.entity.TokenContract
import finance.tegro.api.entity.TokenMetadata
import finance.tegro.api.repository.TokenMetadataRepository
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.ton.block.AddrNone
import java.time.Instant

@Component
class FetchTokenMetadataProcessor(
    private val tokenMetadataRepository: TokenMetadataRepository,
) : ItemProcessor<TokenContract, TokenMetadata> {
    override fun process(contract: TokenContract): TokenMetadata {
        val metadata = if (contract.address != AddrNone) {
            runBlocking {
                JettonMetadata.of(
                    contract.content,
                    WebClient.create(),
                )
            }
        } else {
            // TODO: Put default metadata into contract's content
            JettonMetadata(
                uri = null,
                name = "Toncoin",
                description = "The native currency of the TON blockchain",
                image = "https://ton.org/download/ton_symbol.svg",
                imageData = null,
                symbol = "TON",
                decimals = 9,
            )
        }

        return tokenMetadataRepository.findByAddress(contract.address).orElse(null)
            ?.apply {
                this.uri = metadata.uri
                this.name = metadata.name
                this.description = metadata.description
                this.image = metadata.image
                this.imageData = metadata.imageData
                this.symbol = metadata.symbol
                this.decimals = metadata.decimals
                this.block = contract.block
                this.timestamp = Instant.now()
            }
            ?: TokenMetadata(
                contract.address,
                metadata.uri,
                metadata.name,
                metadata.description,
                metadata.image,
                metadata.imageData,
                metadata.symbol,
                metadata.decimals,
                contract.block,
                Instant.now()
            )
    }

    companion object : KLogging()
}
