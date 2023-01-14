package finance.tegro.rest.controller

import finance.tegro.core.repository.LiquidityRepository
import finance.tegro.core.toSafeString
import finance.tegro.rest.dto.LiquidityDTO
import finance.tegro.rest.mapper.EntityMapper
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.ton.block.MsgAddress

@RestController
@RequestMapping("/v1/liquidity")
class LiquidityController(
    private val entityMapper: EntityMapper,

    private val liquidityRepository: LiquidityRepository,
) {
    @GetMapping("/{account}")
    fun getAllAccountLiquidity(@PathVariable account: MsgAddress): List<LiquidityDTO> =
        liquidityRepository.findDistinctExchangePairAddressByOwner(account)
            .mapNotNull { exchangePair ->
                try {
                    getAccountPairLiquidity(account, exchangePair)
                } catch (_: Exception) {
                    null
                }
            }

    @GetMapping("/{account}/{pair}")
    fun getAccountPairLiquidity(@PathVariable account: MsgAddress, @PathVariable pair: MsgAddress): LiquidityDTO =
        liquidityRepository.findFirstByOwnerAndExchangePairAddressOrderByBlockId_TimestampDesc(account, pair)
            .orElseThrow { throw IllegalArgumentException("Liquidity ${account.toSafeString()} of ${pair.toSafeString()} not found") }
            .let { entityMapper.toDTO(it) }
}
