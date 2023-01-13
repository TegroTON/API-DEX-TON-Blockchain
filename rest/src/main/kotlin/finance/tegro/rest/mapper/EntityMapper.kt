package finance.tegro.rest.mapper

import finance.tegro.core.entity.ExchangePair
import finance.tegro.core.entity.Liquidity
import finance.tegro.core.entity.Reserve
import finance.tegro.core.entity.Token
import finance.tegro.rest.dto.ExchangePairDTO
import finance.tegro.rest.dto.LiquidityDTO
import finance.tegro.rest.dto.ReserveDTO
import finance.tegro.rest.dto.TokenDTO
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface EntityMapper {
    @Mapping(target = "address", source = "exchangePair.address")
    @Mapping(target = "timestamp", source = "exchangePair.timestamp")
    @Mapping(target = "liquidity", source = "exchangePair.token.liquidityToken")
    @Mapping(target = "base", source = "exchangePair.token.baseToken")
    @Mapping(target = "quote", source = "exchangePair.token.quoteToken")
    @Mapping(target = "tokenTimestamp", source = "exchangePair.token.timestamp")
    @Mapping(target = "reserve", source = "reserve")
    fun toDTO(exchangePair: ExchangePair, reserve: Reserve): ExchangePairDTO

    @Mapping(target = "address", source = "address")
    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "exchangePair", source = "exchangePairAddress")
    @Mapping(target = "liquidityToken", source = "exchangePair.token.liquidityToken")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "timestamp", source = "timestamp")
    fun toDTO(liquidity: Liquidity): LiquidityDTO

    @Mapping(target = "address", source = "address")
    @Mapping(target = "base", source = "base")
    @Mapping(target = "quote", source = "quote")
    @Mapping(target = "timestamp", source = "timestamp")
    fun toDTO(reserve: Reserve): ReserveDTO

    @Mapping(target = "address", source = "address")
    @Mapping(target = "timestamp", source = "timestamp")
    @Mapping(target = "totalSupply", source = "contract.totalSupply")
    @Mapping(target = "mintable", source = "contract.mintable")
    @Mapping(target = "admin", source = "contract.admin")
    @Mapping(target = "contractTimestamp", source = "contract.timestamp")
    @Mapping(target = "name", source = "metadata.name")
    @Mapping(target = "description", source = "metadata.description")
    @Mapping(target = "symbol", source = "metadata.symbol")
    @Mapping(target = "decimals", source = "metadata.decimals")
    @Mapping(target = "metadataTimestamp", source = "metadata.timestamp")
    fun toDTO(token: Token): TokenDTO
}
