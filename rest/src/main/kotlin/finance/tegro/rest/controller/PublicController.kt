package finance.tegro.rest.controller

import finance.tegro.core.repository.ExchangePairRepository
import finance.tegro.core.repository.ReserveRepository
import finance.tegro.core.repository.SwapRepository
import finance.tegro.core.toSafeString
import finance.tegro.rest.dto.PublicSummaryDTO
import mu.KLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.Instant

@RestController
@RequestMapping("/public/v1")
class PublicController(
    private val exchangePairRepository: ExchangePairRepository,
    private val reserveRepository: ReserveRepository,
    private val swapRepository: SwapRepository,
) {
    @GetMapping("/dex")
    fun getSummary(): Map<String, PublicSummaryDTO> =
        exchangePairRepository.findAll()
            .mapNotNull { exchangePair ->
                try {
                    requireNotNull(exchangePair.address.toSafeString()) to PublicSummaryDTO(
                        url = "https://tegro.finance/swap?from=${exchangePair.token?.baseToken?.address?.toSafeString()}&to=${exchangePair.token?.quoteToken?.address?.toSafeString()}",
                        baseId = requireNotNull(exchangePair.token?.baseToken?.address) { "Base token of ${exchangePair.address.toSafeString()} not found" },
                        baseName = requireNotNull(exchangePair.token?.baseToken?.metadata?.name) { "Base token name of ${exchangePair.address.toSafeString()} not found" },
                        baseSymbol = requireNotNull(exchangePair.token?.baseToken?.metadata?.symbol) { "Base token symbol of ${exchangePair.address.toSafeString()} not found" },
                        baseVolume = swapRepository.findBaseVolume(exchangePair).orElse(BigDecimal.ZERO),
                        quoteId = requireNotNull(exchangePair.token?.quoteToken?.address) { "Quote token of ${exchangePair.address.toSafeString()} not found" },
                        quoteName = requireNotNull(exchangePair.token?.quoteToken?.metadata?.name) { "Quote token name of ${exchangePair.address.toSafeString()} not found" },
                        quoteSymbol = requireNotNull(exchangePair.token?.quoteToken?.metadata?.symbol) { "Quote token symbol of ${exchangePair.address.toSafeString()} not found" },
                        quoteVolume = swapRepository.findQuoteVolume(exchangePair).orElse(BigDecimal.ZERO),
                        lastPrice = swapRepository.findPriceOn(exchangePair, Instant.now())
                            .orElseThrow { throw IllegalArgumentException("Price of ${exchangePair.address.toSafeString()} now not found") },
                    )
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to get summary for ${exchangePair.token?.baseToken?.metadata?.symbol}_${exchangePair.token?.quoteToken?.metadata?.symbol}" }
                    null
                }
            }
            .associate { it }

    companion object : KLogging()
}
