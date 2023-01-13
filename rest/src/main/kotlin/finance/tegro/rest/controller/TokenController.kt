package finance.tegro.rest.controller

import finance.tegro.core.repository.TokenRepository
import finance.tegro.core.toSafeString
import finance.tegro.rest.dto.TokenDTO
import finance.tegro.rest.mapper.EntityMapper
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.ton.block.MsgAddress

@RestController
class TokenController(
    private val entityMapper: EntityMapper,

    private val tokenRepository: TokenRepository,
) {
    @GetMapping("/token")
    fun getAllTokens(): List<TokenDTO> =
        tokenRepository.findAll()
            .mapNotNull {
                try {
                    getTokenByAddress(it.address)
                } catch (_: Exception) {
                    null
                }
            }

    @GetMapping("/token/{address}")
    fun getTokenByAddress(@PathVariable address: MsgAddress): TokenDTO =
        tokenRepository.findByAddress(address)
            .orElseThrow { throw IllegalArgumentException("Token ${address.toSafeString()} not found") }
            .let { entityMapper.toDTO(it) }
}
