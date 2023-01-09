package finance.tegro.api.controller

import finance.tegro.api.dto.LiquidityDTO
import finance.tegro.api.dto.TotalLiquidityDTO
import finance.tegro.api.mapper.LiquidityMapper
import finance.tegro.api.repository.LiquidityRepository
import finance.tegro.api.repository.TokenContractRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.ton.block.MsgAddress

@RestController
class LiquidityController(
    private val liquidityMapper: LiquidityMapper,

    private val liquidityRepository: LiquidityRepository,
    private val tokenContractRepository: TokenContractRepository,
) {
    @GetMapping("/liquidity/{pair}")
    fun getTotalLiquidity(@PathVariable pair: MsgAddress): TotalLiquidityDTO =
        tokenContractRepository.findByAddress(pair)
            .orElseThrow { throw IllegalArgumentException("Token contract $pair not found") }
            .let { liquidityMapper.tokenContractToTotalLiquidityDTO(it) }

    @GetMapping("/liquidity/{pair}/{account}")
    fun getAccountLiquidity(@PathVariable pair: MsgAddress, @PathVariable account: MsgAddress): LiquidityDTO =
        liquidityRepository.findTopByOwnerAndExchangePairOrderByTimestampDesc(account, pair)
            .orElseThrow { throw IllegalArgumentException("Liquidity $account of $pair not found") }
            .let { liquidityMapper.liquidityToLiquidityDTO(it) }
}
