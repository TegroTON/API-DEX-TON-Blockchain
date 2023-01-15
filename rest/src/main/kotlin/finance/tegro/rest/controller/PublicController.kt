package finance.tegro.rest.controller

import finance.tegro.core.repository.ExchangePairRepository
import finance.tegro.core.repository.ReserveRepository
import finance.tegro.core.repository.SwapRepository
import finance.tegro.core.toSafeString
import finance.tegro.rest.dto.PublicSummaryDTO
import finance.tegro.rest.dto.PublicTickerDTO
import finance.tegro.rest.dto.PublicTradeDTO
import mu.KLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.Instant
import java.time.temporal.ChronoUnit

@RestController
@RequestMapping("/public/v1")
class PublicController(
    private val exchangePairRepository: ExchangePairRepository,
    private val reserveRepository: ReserveRepository,
    private val swapRepository: SwapRepository,
) {
    @GetMapping
    fun getSummary(): List<PublicSummaryDTO> =
        exchangePairRepository.findAll()
            .mapNotNull { exchangePair ->
                try {
                    val price24hAgo =
                        swapRepository.findPriceOn(exchangePair, Instant.now().minus(24, ChronoUnit.HOURS))
                            .orElseThrow { throw IllegalArgumentException("Price of ${exchangePair.address.toSafeString()} 24hr ago not found") }
                    val lastPrice = swapRepository.findPriceOn(exchangePair, Instant.now())
                        .orElseThrow { throw IllegalArgumentException("Price of ${exchangePair.address.toSafeString()} now not found") }

                    PublicSummaryDTO(
                        tradingPairs = requireNotNull(exchangePair.token?.baseToken?.metadata?.symbol) { "Base token symbol is null" } + "_" +
                                requireNotNull(exchangePair.token?.quoteToken?.metadata?.symbol) { "Quote token name is null" },
                        lastPrice = lastPrice,
                        baseVolume = swapRepository.findBaseVolume(exchangePair)
                            .orElseThrow { throw IllegalArgumentException("Base volume of ${exchangePair.address.toSafeString()} not found") },
                        quoteVolume = swapRepository.findQuoteVolume(exchangePair)
                            .orElseThrow { throw IllegalArgumentException("Quote volume of ${exchangePair.address.toSafeString()} not found") },
                        priceChangePercent24h = (lastPrice - price24hAgo) / price24hAgo * BigDecimal(100),
                        highestPrice24h = swapRepository.findMaxPrice(
                            exchangePair,
                            Instant.now().minus(24, ChronoUnit.HOURS)
                        )
                            .orElseThrow { throw IllegalArgumentException("Max price of ${exchangePair.address.toSafeString()} not found") },
                        lowestPrice24h = swapRepository.findMinPrice(
                            exchangePair,
                            Instant.now().minus(24, ChronoUnit.HOURS)
                        )
                            .orElseThrow { throw IllegalArgumentException("Min price of ${exchangePair.address.toSafeString()} not found") },
                    )
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to get summary for ${exchangePair.token?.baseToken?.metadata?.symbol}_${exchangePair.token?.quoteToken?.metadata?.symbol}" }
                    null
                }
            }

    @GetMapping("/ticker")
    fun getTickers(): Map<String, PublicTickerDTO> =
        exchangePairRepository.findAll()
            .mapNotNull { exchangePair ->
                try {
                    (requireNotNull(exchangePair.token?.baseToken?.metadata?.symbol) { "Base token symbol is null" } + "_" +
                            requireNotNull(exchangePair.token?.quoteToken?.metadata?.symbol) { "Quote token name is null" }) to
                            PublicTickerDTO(
                                lastPrice = swapRepository.findPriceOn(exchangePair, Instant.now())
                                    .orElseThrow { throw IllegalArgumentException("Price of ${exchangePair.address.toSafeString()} now not found") },
                                baseVolume = swapRepository.findBaseVolume(exchangePair)
                                    .orElseThrow { throw IllegalArgumentException("Base volume of ${exchangePair.address.toSafeString()} not found") },
                                quoteVolume = swapRepository.findQuoteVolume(exchangePair)
                                    .orElseThrow { throw IllegalArgumentException("Quote volume of ${exchangePair.address.toSafeString()} not found") },
                            )
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to get ticker for ${exchangePair.token?.baseToken?.metadata?.symbol}_${exchangePair.token?.quoteToken?.metadata?.symbol}" }
                    null
                }
            }
            .associate { it }

    @GetMapping("/trades/{marketPair}")
    fun getTrades(@PathVariable marketPair: String): List<PublicTradeDTO> {
        val (baseTokenSymbol, quoteTokenSymbol) = marketPair.split("_")
        val exchangePair = exchangePairRepository.findByBaseAndQuoteTokenSymbols(baseTokenSymbol, quoteTokenSymbol)
            .orElseThrow { throw IllegalArgumentException("Exchange pair $marketPair not found") }

        return swapRepository.findByExchangePair(exchangePair, Pageable.ofSize(1000))
            .mapNotNull { swap ->
                try {
                    PublicTradeDTO(
                        tradeId = requireNotNull(swap.id) { "Swap id is null" }.leastSignificantBits,
                        price = (BigDecimal(
                            swap.quoteAmount,
                            requireNotNull(swap.exchangePair?.token?.quoteToken?.metadata?.decimals) { "Quote token decimals is null" })
                            .divide(
                                BigDecimal(
                                    swap.baseAmount,
                                    requireNotNull(swap.exchangePair?.token?.baseToken?.metadata?.decimals) { "Base token decimals is null" }),
                                RoundingMode.UP
                            )
                            .round(MathContext(requireNotNull(swap.exchangePair?.token?.quoteToken?.metadata?.decimals) { "Quote token decimals is null" }))),
                        baseVolume = BigDecimal(
                            swap.baseAmount,
                            requireNotNull(swap.exchangePair?.token?.baseToken?.metadata?.decimals) { "Base token decimals is null" }),
                        quoteVolume = BigDecimal(
                            swap.quoteAmount,
                            requireNotNull(swap.exchangePair?.token?.quoteToken?.metadata?.decimals) { "Quote token decimals is null" }),
                        timestamp = swap.timestamp,
                        type = if (swap.inverse) "buy" else "sell",
                    )
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to get trade for ${swap.exchangePair?.token?.baseToken?.metadata?.symbol}_${swap.exchangePair?.token?.quoteToken?.metadata?.symbol}" }
                    null
                }
            }
    }

    companion object : KLogging()
}
