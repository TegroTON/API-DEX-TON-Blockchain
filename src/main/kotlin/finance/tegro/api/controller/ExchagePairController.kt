package finance.tegro.api.controller

import finance.tegro.api.dto.ExchangePairDTO
import finance.tegro.api.mapper.ExchangePairMapper
import finance.tegro.api.repository.ExchangePairRepository
import finance.tegro.api.repository.ExchangePairTokenRepository
import finance.tegro.api.repository.ReserveRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.ton.block.MsgAddress

@RestController
class ExchagePairController(
    private val exchangePairMapper: ExchangePairMapper,

    private val exchangePairRepository: ExchangePairRepository,
    private val exchangePairTokenRepository: ExchangePairTokenRepository,
    private val reserveRepository: ReserveRepository,
) {
    @GetMapping("/pair")
    fun getAllPairs(): List<ExchangePairDTO> =
        exchangePairRepository.findAll()
            .mapNotNull {
                try {
                    getPairByAddress(it.address)
                } catch (_: Exception) {
                    null
                }
            }

    @GetMapping("/pair/{address}")
    fun getPairByAddress(@PathVariable address: MsgAddress): ExchangePairDTO {
        val exchangePair = exchangePairRepository.findByAddress(address)
            .orElseThrow { throw IllegalArgumentException("Exchange pair $address not found") }
        val exchangePairToken = exchangePairTokenRepository.findByAddress(address)
            .orElseThrow { throw IllegalArgumentException("Exchange pair token $address not found") }
        val reserve = reserveRepository.findTopByAddressOrderByTimestampDesc(address)
            .orElseThrow { throw IllegalArgumentException("Reserve $address not found") }

        return exchangePairMapper.exchangePairToExchangePairDTO(exchangePair, exchangePairToken, reserve)
    }

    @GetMapping("/pair/token/{base}/{quote}")
    fun getPairByTokenAddresses(@PathVariable base: MsgAddress, @PathVariable quote: MsgAddress): ExchangePairDTO =
        exchangePairTokenRepository.findByBaseAndQuote(base, quote).orElse(null)
            ?.let { return getPairByAddress(it.address) }
            ?: exchangePairTokenRepository.findByBaseAndQuote(quote, base) // Find inverse pair
                .orElseThrow { throw IllegalArgumentException("Exchange pair $base/$quote not found") }
                .let { getPairByAddress(it.address).inverse() }
}
