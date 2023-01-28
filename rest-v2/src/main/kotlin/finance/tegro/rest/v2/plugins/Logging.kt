package finance.tegro.rest.v2.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import org.slf4j.event.Level

fun Application.configureLogging() {
    install(CallLogging) {
        level = Level.INFO
        format {
            val status = it.response.status()?.toString() ?: "unhandled"
            "$status: ${it.request.httpMethod.value} - ${it.request.path()} - ${it.request.userAgent()}"
        }
    }
}
