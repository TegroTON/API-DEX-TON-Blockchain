package finance.tegro.tonindexer.services

import io.ktor.util.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import org.ton.api.liteserver.LiteServerDesc
import org.ton.api.pub.PublicKeyEd25519
import org.ton.lite.client.LiteClient
import kotlin.coroutines.CoroutineContext

@OptIn(DelicateCoroutinesApi::class)
object TonLiteApiService : CoroutineScope {
    override val coroutineContext: CoroutineContext = newSingleThreadContext(toString()) + CoroutineName(toString())

    lateinit var liteClient: LiteClient
    val liteApi get() = liteClient.liteApi

    fun init(
        ip: Int = -1182143975,
        port: Int = 36573,
        key: String = "ZYe6sDZZc63sH3JnpIS8Yj86NFGnRlQF9NOvzl6UM0c="
    ) {
        liteClient = LiteClient(
            coroutineContext,
            LiteServerDesc(
                id = PublicKeyEd25519(key.decodeBase64Bytes()),
                ip = ip,
                port = port
            )
        )
    }
}
