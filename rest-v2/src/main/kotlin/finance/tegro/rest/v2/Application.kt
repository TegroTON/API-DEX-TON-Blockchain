package finance.tegro.rest.v2

import finance.tegro.rest.v2.models.DatabaseFactory
import finance.tegro.rest.v2.models.ExchangePairFacade
import finance.tegro.rest.v2.models.StaticExchangePairFacade
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
    DatabaseFactory.init()
    // TODO: move to ydb
    exchangePairsFacade = StaticExchangePairFacade(
        "0:DF567A477B0B6878EB1FFABB5421322278FD96033234ECEA61E5D2C6740BE533",
        "0:E6D5C9A921C9B35D9A5D3C6DDBB2E0AAC0279EFC7A08449D5E778AA4DDAC28C5",
        "0:CEE77FCF441F60A57A8412179C6B8F1DD8AF46E57C7C915C5F61735B856F89FF",
        "0:991454B081FB77EE00DF71189C1ECB91172FE8C608D390E73D68AA404CCB8F34",
        "0:7BC95AEB12A6EB120DB60067B1C56089D54B822D6B7A764859E9F16A273B7C1F",
        "0:4680F033A97EF62F2780F10AA97C243C222F25F8A80751E27DC3F8E034F3309D",
        "0:D38D6B899B00F9489F25F0BD33A0DCBC9893F8689FE4E9BE04836BE6F3C942A4",
        "0:987CA2F96DBFDAA7F14D9991D057547CF49BB6F4E08E0BE3AFBAF59406B4FB6A",
        "0:36F5077274A44F00AB7AACF4C8998D00B940FA3BB8586D56290E8745024CEE30",
        "0:87E43D4419DE3C40548B5ADE3FEE3E67A03042C6E801D1F5E2EE17DE0C4B2274",
        "0:A80C4725CFA33454F31960560133FFB6047B3A37E011BFFF966F630DBB197CF5",
        "0:295641A76C941E314DEC93F501402D65E54DAA9FEE66359EB7170374EDF56D93",
        "0:FC4EB14842F26116091B224D063B68EEE175A7F8BA4F7E92D693A081245C29CB",
        "0:98F8528675B0BC1948757517709BB45C9357993A781DF30DB2447A266FB5D235",
        "0:AB3545ACE6862003F670BF253C86D6DFADF8D857E02FFA1B4C5C4D1843838FFD",
        "0:223da9ba33802eb7f8d84bb41586d61e4d7af1e7e16db78485c82cd6e6eec697",
        "0:2f4024649628825d6fc047f6180278a0537f070d91e22b45b0faf4f10ba0e779",
        "0:c5253a5a855415d762a62fd5f2ce625c00c32a32dc2e2b89714e2d90d318e065",
        "0:1034e36b0752fe20fd523d226b3b953f765aebdfa5ed9aba2df0fe9934189758",
        "0:1765004eb7b5beacb289088f43c072d2a7b01dc1029c8c7324a081323ea15634"
    )
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
