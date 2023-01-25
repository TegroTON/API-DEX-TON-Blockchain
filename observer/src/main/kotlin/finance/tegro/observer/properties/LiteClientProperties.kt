package finance.tegro.observer.properties

import io.ktor.util.*
import org.springframework.boot.context.properties.ConfigurationProperties
import org.ton.api.liteserver.LiteServerDesc
import org.ton.api.pub.PublicKeyEd25519

@ConfigurationProperties(prefix = "lite.client")
data class LiteClientProperties(
    val ip: Int,

    val port: Int,

    val key: String,
) {
    fun toLiteServerDesc() = LiteServerDesc(
        ip = ip,
        port = port,
        id = PublicKeyEd25519(key.decodeBase64Bytes()),
    )
}
