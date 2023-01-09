package finance.tegro.api.mapper

import finance.tegro.api.dto.LiquidityDTO
import finance.tegro.api.dto.TotalLiquidityDTO
import finance.tegro.api.entity.Liquidity
import finance.tegro.api.entity.TokenContract
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface LiquidityMapper {
    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "exchangePair", source = "exchangePair")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "timestamp", source = "timestamp")
    fun liquidityToLiquidityDTO(liquidity: Liquidity): LiquidityDTO

    @Mapping(target = "exchangePair", source = "address")
    @Mapping(target = "liquidity", source = "totalSupply")
    @Mapping(target = "timestamp", source = "timestamp")
    fun tokenContractToTotalLiquidityDTO(tokenContract: TokenContract): TotalLiquidityDTO
}
