package finance.tegro.api.mapper

import finance.tegro.api.dto.ExchangePairDTO
import finance.tegro.api.entity.ExchangePair
import finance.tegro.api.entity.ExchangePairToken
import finance.tegro.api.entity.Reserve
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface ExchangePairMapper {
    @Mapping(target = "id", source = "exchangePair.id")
    @Mapping(target = "address", source = "exchangePair.address")
    @Mapping(target = "timestamp", source = "exchangePair.timestamp")
    @Mapping(target = "tokenBase", source = "exchangePairToken.base")
    @Mapping(target = "tokenQuote", source = "exchangePairToken.quote")
    @Mapping(target = "tokenTimestamp", source = "exchangePairToken.timestamp")
    @Mapping(target = "reserveBase", source = "reserve.base")
    @Mapping(target = "reserveQuote", source = "reserve.quote")
    @Mapping(target = "reserveTimestamp", source = "reserve.timestamp")
    fun exchangePairToExchangePairDTO(
        exchangePair: ExchangePair,
        exchangePairToken: ExchangePairToken,
        reserve: Reserve
    ): ExchangePairDTO
}
