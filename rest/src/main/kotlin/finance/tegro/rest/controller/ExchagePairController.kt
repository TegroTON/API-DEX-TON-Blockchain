package finance.tegro.rest.controller

import finance.tegro.core.repository.ExchangePairRepository
import finance.tegro.core.repository.ExchangePairTokenRepository
import finance.tegro.core.toSafeString
import finance.tegro.rest.dto.ExchangePairDTO
import finance.tegro.rest.mapper.EntityMapper
import mu.KLogging
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.ton.block.MsgAddress

@RestController
@RequestMapping("/v1/pair")
class ExchagePairController(
    private val entityMapper: EntityMapper,

    private val exchangePairRepository: ExchangePairRepository,
    private val exchangePairTokenRepository: ExchangePairTokenRepository,
) {
    @Cacheable("pairs")
    @GetMapping
    fun getAllPairs(): List<ExchangePairDTO> =
        exchangePairRepository.findAll()
            .mapNotNull {
                try {
                    getPairByAddress(it.address)
                } catch (_: Exception) {
                    null
                }
            }

    @CacheEvict("pairs", allEntries = true)
    @Scheduled(fixedRateString = "\${observer.cache.pairs:1000}")
    fun evictAllPairs() {
        logger.debug { "Evicting all pairs" }
    }

    @Cacheable("pairs_by_address")
    @GetMapping("/{address}")
    fun getPairByAddress(@PathVariable address: MsgAddress): ExchangePairDTO {
        val exchangePair = exchangePairRepository.findByAddress(address)
            .orElseThrow { throw IllegalArgumentException("Exchange pair ${address.toSafeString()} not found") }
        return entityMapper.toDTO(exchangePair)
    }

    @CacheEvict("pairs_by_address", allEntries = true)
    @Scheduled(fixedRateString = "\${observer.cache.pairs_by_address:1000}")
    fun evictAllPairsByAddress() {
        logger.debug { "Evicting all pairs by address" }
    }

    @Cacheable("pairs_by_token")
    @GetMapping("/token/{base}/{quote}")
    fun getPairByTokenAddresses(@PathVariable base: MsgAddress, @PathVariable quote: MsgAddress): ExchangePairDTO =
        exchangePairTokenRepository.findByBaseAndQuote(base, quote).orElse(null)
            ?.let { return getPairByAddress(it.address) }
            ?: exchangePairTokenRepository.findByBaseAndQuote(quote, base) // Find inverse pair
                .orElseThrow { throw IllegalArgumentException("Exchange pair ${base.toSafeString()}/${quote.toSafeString()} not found") }
                .let { getPairByAddress(it.address).inverse() }

    @CacheEvict("pairs_by_token", allEntries = true)
    @Scheduled(fixedRateString = "\${observer.cache.pairs_by_token:1000}")
    fun evictAllPairsByToken() {
        logger.debug { "Evicting all pairs by token" }
    }

    companion object : KLogging()
}
