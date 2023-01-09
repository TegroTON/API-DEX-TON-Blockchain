package finance.tegro.api.mapper

import finance.tegro.api.dto.TokenDTO
import finance.tegro.api.entity.Token
import finance.tegro.api.entity.TokenContract
import finance.tegro.api.entity.TokenMetadata
import io.ktor.util.*
import org.apache.tika.Tika
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
abstract class TokenMapper {
    @Mapping(target = "id", source = "token.id")
    @Mapping(target = "address", source = "token.address")
    @Mapping(target = "timestamp", source = "token.timestamp")
    @Mapping(target = "totalSupply", source = "tokenContract.totalSupply")
    @Mapping(target = "mintable", source = "tokenContract.mintable")
    @Mapping(target = "admin", source = "tokenContract.admin")
    @Mapping(target = "contractTimestamp", source = "tokenContract.timestamp")
    @Mapping(target = "name", source = "tokenMetadata.name")
    @Mapping(target = "description", source = "tokenMetadata.description")
    @Mapping(target = "image", source = "tokenMetadata")
    @Mapping(target = "symbol", source = "tokenMetadata.symbol")
    @Mapping(target = "decimals", source = "tokenMetadata.decimals")
    @Mapping(target = "metadataTimestamp", source = "tokenMetadata.timestamp")
    abstract fun tokenToTokenDTO(token: Token, tokenContract: TokenContract, tokenMetadata: TokenMetadata): TokenDTO

    fun mapImage(tokenMetadata: TokenMetadata): String? =
        tokenMetadata.image
            ?: tokenMetadata.imageData?.let { "data:${detectImageMimeType(it)};base64,${it.encodeBase64()}" }

    private fun detectImageMimeType(imageData: ByteArray): String? {
        val type = Tika().detect(imageData)
        return if (type == "text/plain") {
            "image/svg+xml" // Fucking piece of shit
        } else {
            type
        }
    }
}
