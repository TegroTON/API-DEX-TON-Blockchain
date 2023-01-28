package finance.tegro.rest.v2

import finance.tegro.rest.v2.plugins.configureLogging
import finance.tegro.rest.v2.plugins.configureRouting
import finance.tegro.rest.v2.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.cio.*

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    configureLogging()
    configureSerialization()
    configureRouting()
}
