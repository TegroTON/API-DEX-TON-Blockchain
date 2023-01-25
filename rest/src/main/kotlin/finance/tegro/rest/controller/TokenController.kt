package finance.tegro.rest.controller

import finance.tegro.core.repository.TokenRepository
import finance.tegro.core.toSafeString
import finance.tegro.rest.dto.TokenDTO
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
@RequestMapping("/v1/token")
class TokenController(
    private val entityMapper: EntityMapper,

    private val tokenRepository: TokenRepository,
) {
    @Cacheable("tokens")
    @GetMapping
    fun getAllTokens(): List<TokenDTO> =
        tokenRepository.findAll()
            .mapNotNull {
                try {
                    getTokenByAddress(it.address)
                } catch (_: Exception) {
                    null
                }
            }

    @CacheEvict("tokens", allEntries = true)
    @Scheduled(fixedRateString = "\${observer.cache.tokens:30000}")
    fun evictAllTokens() {
        logger.debug { "Evicting all tokens" }
    }

    @Cacheable("tokens_by_address")
    @GetMapping("/{address}")
    fun getTokenByAddress(@PathVariable address: MsgAddress): TokenDTO =
        tokenRepository.findByAddress(address)
            .orElseThrow { throw IllegalArgumentException("Token ${address.toSafeString()} not found") }
            .let { entityMapper.toDTO(it) }

    @CacheEvict("tokens_by_address", allEntries = true)
    @Scheduled(fixedRateString = "\${observer.cache.tokens_by_address:30000}")
    fun evictAllTokensByAddress() {
        logger.debug { "Evicting all tokens by address" }
    }


    companion object : KLogging()
}
