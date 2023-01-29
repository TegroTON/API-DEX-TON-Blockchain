package finance.tegro.rest.v2

import finance.tegro.rest.v2.models.DatabaseFactory
import finance.tegro.rest.v2.models.ExchangePairFacade
import finance.tegro.rest.v2.plugins.configureLogging
import finance.tegro.rest.v2.plugins.configureRouting
import finance.tegro.rest.v2.plugins.configureSerialization
import finance.tegro.rest.v2.services.ExchangePairsStateService
import finance.tegro.rest.v2.services.MasterchainBlockService
import finance.tegro.rest.v2.services.ReservesService
import finance.tegro.rest.v2.services.TonLiteApiService
import io.ktor.server.application.*
import io.ktor.server.cio.*

fun main(args: Array<String>) = EngineMain.main(args)

lateinit var exchangePairsFacade: ExchangePairFacade

fun Application.module() {
    DatabaseFactory.init()
    exchangePairsFacade = ExchangePairFacade.cached()
    TonLiteApiService.init()
    MasterchainBlockService.init()
    ExchangePairsStateService.init()
    ReservesService.init()

    configureLogging()
    configureSerialization()
    configureRouting()
}
