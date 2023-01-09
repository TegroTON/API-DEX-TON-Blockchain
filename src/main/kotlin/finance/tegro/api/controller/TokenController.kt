package finance.tegro.api.controller

import finance.tegro.api.dto.TokenDTO
import finance.tegro.api.mapper.TokenMapper
import finance.tegro.api.repository.TokenContractRepository
import finance.tegro.api.repository.TokenMetadataRepository
import finance.tegro.api.repository.TokenRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.ton.block.MsgAddress

@RestController
class TokenController(
    private val tokenMapper: TokenMapper,

    private val tokenRepository: TokenRepository,
    private val tokenContractRepository: TokenContractRepository,
    private val tokenMetadataRepository: TokenMetadataRepository
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
    fun getTokenByAddress(@PathVariable address: MsgAddress): TokenDTO {
        val token = tokenRepository.findByAddress(address)
            .orElseThrow { throw IllegalArgumentException("Token $address not found") }
        val tokenContract = tokenContractRepository.findByAddress(address)
            .orElseThrow { throw IllegalArgumentException("Token contract $address not found") }
        val tokenMetadata = tokenMetadataRepository.findByAddress(address)
            .orElseThrow { throw IllegalArgumentException("Token metadata $address not found") }

        return tokenMapper.tokenToTokenDTO(token, tokenContract, tokenMetadata)
    }
}
