package finance.tegro.rest.v2

import finance.tegro.rest.v2.models.DatabaseFactory
import finance.tegro.rest.v2.models.ExchangePairFacade
import finance.tegro.rest.v2.plugins.configureLogging
import finance.tegro.rest.v2.plugins.configureRouting
import finance.tegro.rest.v2.plugins.configureSerialization
import finance.tegro.rest.v2.services.*
import finance.tegro.tonindexer.services.MasterchainBlockService
import finance.tegro.tonindexer.services.TonLiteApiService
import io.ktor.server.application.*
import io.ktor.server.cio.*

fun main(args: Array<String>) = EngineMain.main(args)

lateinit var exchangePairsFacade: ExchangePairFacade

fun Application.module() {
    DatabaseFactory.init(
        url = "jdbc:postgresql://rc1a-a7v2hm0mvex0zty7.mdb.yandexcloud.net:6432/finance?sslmode=require&amp;sslfactory=org.postgresql.ssl.NonValidatingFactory",
        username = "rest",
        password = "Prevent-Outshoot-Parasitic9"
    )
    exchangePairsFacade = ExchangePairFacade.cached()
    TonLiteApiService.init()
    MasterchainBlockService.init()
    AccountStatesService.init()
    ReservesService.init()
    PairsService.init()
    JettonDataService.init()
    PairV1CacheService.init()

    configureLogging()
    configureSerialization()
    configureRouting()
}
