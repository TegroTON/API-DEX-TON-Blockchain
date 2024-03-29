package finance.tegro.rest.controller

import finance.tegro.core.repository.ExchangePairRepository
import finance.tegro.core.repository.SwapRepository
import finance.tegro.core.toMsgAddress
import finance.tegro.core.toSafeString
import finance.tegro.rest.dto.PublicDexDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import mu.KLogging
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.Instant

@RestController
@RequestMapping("/public/v1")
class PublicController(
    private val exchangePairRepository: ExchangePairRepository,
    private val swapRepository: SwapRepository,
) {
    @Operation(summary = "Get information about lal available exchange pairs on DEX")
    @Cacheable("dexes")
    @GetMapping("/dex")
    fun getAllDexes(): List<PublicDexDTO> =
        exchangePairRepository.findAll()
            .mapNotNull { exchangePair ->
                try {
                    getDex(requireNotNull(exchangePair.address.toSafeString()) { "Exchange pair ${exchangePair.address.toSafeString()} not vaid" })
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to get dex for ${exchangePair.address.toSafeString()}" }
                    null
                }
            }

    @CacheEvict("dexes", allEntries = true)
    @Scheduled(fixedRateString = "\${observer.cache.dexes:60000}")
    fun evictAllDexes() {
        logger.debug { "Evicting all dexes" }
    }

    @Operation(summary = "Get information about specific exchange pair of DEX")
    @Cacheable("dexes_by_address")
    @GetMapping("/dex/{address}")
    fun getDex(@Parameter(description = "Id of the exchange pair") @PathVariable address: String): PublicDexDTO =
        exchangePairRepository.findByAddress(address.toMsgAddress())
            .map { exchangePair ->
                PublicDexDTO(
                    id = requireNotNull(exchangePair.address.toSafeString()) { "Address of ${exchangePair.address.toSafeString()} not valid" },
                    url = "https://tegro.finance/swap?from=${exchangePair.token?.baseToken?.address?.toSafeString()}&to=${exchangePair.token?.quoteToken?.address?.toSafeString()}",
                    baseId = exchangePair.token?.baseToken?.address?.toSafeString(),
                    baseName = requireNotNull(exchangePair.token?.baseToken?.metadata?.name) { "Base token name of ${exchangePair.address.toSafeString()} not found" },
                    baseSymbol = requireNotNull(exchangePair.token?.baseToken?.metadata?.symbol) { "Base token symbol of ${exchangePair.address.toSafeString()} not found" },
                    baseVolume = swapRepository.findBaseVolume(exchangePair).orElse(BigDecimal.ZERO),
                    quoteId = exchangePair.token?.quoteToken?.address?.toSafeString(),
                    quoteName = requireNotNull(exchangePair.token?.quoteToken?.metadata?.name) { "Quote token name of ${exchangePair.address.toSafeString()} not found" },
                    quoteSymbol = requireNotNull(exchangePair.token?.quoteToken?.metadata?.symbol) { "Quote token symbol of ${exchangePair.address.toSafeString()} not found" },
                    quoteVolume = swapRepository.findQuoteVolume(exchangePair).orElse(BigDecimal.ZERO),
                    lastPrice = swapRepository.findPriceOn(exchangePair, Instant.now()).orElse(null),
                )
            }
            .orElseThrow { throw IllegalArgumentException("Exchange pair $address not found") }

    @CacheEvict("dexes_by_address", allEntries = true)
    @Scheduled(fixedRateString = "\${observer.cache.dexes:60000}")
    fun evictAllDexesByAddress() {
        logger.debug { "Evicting all dexes by address" }
    }

    companion object : KLogging()
}
