package finance.tegro.observer.job

import finance.tegro.core.contract.JettonMetadata
import finance.tegro.core.entity.BlockId
import finance.tegro.core.entity.TokenMetadata
import finance.tegro.core.repository.TokenContractRepository
import finance.tegro.core.repository.TokenMetadataRepository
import finance.tegro.core.repository.TokenRepository
import finance.tegro.core.toSafeString
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.ton.block.AddrNone
import org.ton.block.MsgAddress
import java.time.Instant

@Component
class TokenMetadataJob(
    private val tokenContractRepository: TokenContractRepository,
    private val tokenMetadataRepository: TokenMetadataRepository,
    private val tokenRepository: TokenRepository,
) : Job {
    override fun execute(context: JobExecutionContext) {
        val jobData = context.mergedJobDataMap
        val address = jobData["address"] as MsgAddress
        val blockId = jobData["blockId"] as BlockId

        logger.debug { "started for address ${address.toSafeString()} and blockId ${blockId.workchain}:${blockId.shard}:${blockId.seqno}" }

        val tokenContract = tokenContractRepository.findByAddress(address).orElse(null) ?: return
        val metadata = if (address != AddrNone) {
            runBlocking {
                JettonMetadata.of(
                    tokenContract.content,
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

        val tokenMetadata = (tokenMetadataRepository.findByAddress(address).orElse(null)
            ?.apply {
                this.uri = metadata.uri
                this.name = metadata.name
                this.description = metadata.description
                this.image = metadata.image
                this.imageData = metadata.imageData
                this.symbol = metadata.symbol
                this.decimals = metadata.decimals
                this.blockId = blockId
                this.timestamp = Instant.now()
            }
            ?: TokenMetadata(
                address,
                metadata.uri,
                metadata.name,
                metadata.description,
                metadata.image,
                metadata.imageData,
                metadata.symbol,
                metadata.decimals,
                blockId,
                Instant.now(),
            ).also { logger.debug { "TokenMetadata ${it.address.toSafeString()} was created" } })
            .let {
                tokenMetadataRepository.save(it)
            }

        tokenRepository.updateMetadataByAddress(tokenMetadata, tokenMetadata.address)
    }

    companion object : KLogging()
}
