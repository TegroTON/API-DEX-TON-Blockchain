package finance.tegro.rest.controller

import finance.tegro.core.repository.ExchangePairRepository
import finance.tegro.core.repository.ReserveRepository
import finance.tegro.core.repository.TokenRepository
import finance.tegro.core.toSafeString
import finance.tegro.rest.dto.TotalValueLockedDTO
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
@RequestMapping("/v1/tvl")
class TotalValueLockedController(
    private val entityMapper: EntityMapper,
    private val exchangePairRepository: ExchangePairRepository,
    private val tokenRepository: TokenRepository,
    private val reserveRepository: ReserveRepository,
) {
    @Cacheable("tvl")
    @GetMapping
    fun getAllTotalValueLocked(): List<TotalValueLockedDTO> =
        tokenRepository.findAllNonLiquidityTokens()
            .mapNotNull {
                try {
                    getTotalValueLocked(it.address)
                } catch (_: Exception) {
                    null
                }
            }

    @CacheEvict("tvl", allEntries = true)
    @Scheduled(fixedRateString = "\${observer.cache.tvl:30000}")
    fun evictAllTvl() {
        logger.debug { "Evicting all tvl" }
    }

    @Cacheable("tvl_by_address")
    @GetMapping("/{address}")
    fun getTotalValueLocked(@PathVariable address: MsgAddress): TotalValueLockedDTO {
        val token = tokenRepository.findByAddress(address)
            .orElseThrow { throw IllegalArgumentException("Token ${address.toSafeString()} not found") }
        val exchangePairs = exchangePairRepository.findByToken_BaseTokenOrToken_QuoteToken(token)
        return TotalValueLockedDTO(
            token = entityMapper.toDTO(token),
            valueLocked = exchangePairs.mapNotNull {
                val reserve = reserveRepository.findFirstByAddressOrderByBlockId_TimestampDesc(it.address).orElse(null)
                if (it.token?.baseToken == token) {
                    reserve?.base
                } else if (it.token?.quoteToken == token) {
                    reserve?.quote
                } else {
                    null
                }
            }
                .sumOf { it },
            pairs = exchangePairs.map { it.address },
        )
    }

    @CacheEvict("tvl_by_address", allEntries = true)
    @Scheduled(fixedRateString = "\${observer.cache.tvl_by_address:30000}")
    fun evictAllTvlByAddress() {
        logger.debug { "Evicting all tvls by address" }
    }

    companion object : KLogging()
}
