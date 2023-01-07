package finance.tegro.api.converter

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ton.api.tonnode.TonNodeBlockIdExt
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter(autoApply = true)
class TonNodeBlockIdExtConverter : AttributeConverter<TonNodeBlockIdExt, String> {
    override fun convertToDatabaseColumn(tonNodeBlockIdExt: TonNodeBlockIdExt): String =
        Json.encodeToString(tonNodeBlockIdExt)

    override fun convertToEntityAttribute(string: String): TonNodeBlockIdExt = Json.decodeFromString(string)
}
