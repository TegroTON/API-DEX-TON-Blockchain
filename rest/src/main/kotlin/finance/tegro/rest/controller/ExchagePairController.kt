package finance.tegro.rest.controller

import finance.tegro.core.repository.ExchangePairRepository
import finance.tegro.core.repository.ExchangePairTokenRepository
import finance.tegro.core.repository.ReserveRepository
import finance.tegro.core.toSafeString
import finance.tegro.rest.dto.ExchangePairDTO
import finance.tegro.rest.mapper.EntityMapper
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.ton.block.MsgAddress

@RestController
class ExchagePairController(
    private val entityMapper: EntityMapper,

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
            .orElseThrow { throw IllegalArgumentException("Exchange pair ${address.toSafeString()} not found") }
        val reserve =
            reserveRepository.findFirstByExchangePairAddressOrderByBlockId_TimestampDesc(exchangePair.address)
                .orElseThrow { throw IllegalArgumentException("Reserve of ${address.toSafeString()} not found") }
        return entityMapper.toDTO(exchangePair, reserve)
    }

    @GetMapping("/pair/token/{base}/{quote}")
    fun getPairByTokenAddresses(@PathVariable base: MsgAddress, @PathVariable quote: MsgAddress): ExchangePairDTO =
        exchangePairTokenRepository.findByBaseAndQuote(base, quote).orElse(null)
            ?.let { return getPairByAddress(it.address) }
            ?: exchangePairTokenRepository.findByBaseAndQuote(quote, base) // Find inverse pair
                .orElseThrow { throw IllegalArgumentException("Exchange pair ${base.toSafeString()}/${quote.toSafeString()} not found") }
                .let { getPairByAddress(it.address).inverse() }
}
