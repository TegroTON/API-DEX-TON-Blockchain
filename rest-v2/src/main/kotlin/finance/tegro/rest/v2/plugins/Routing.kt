package finance.tegro.rest.v2.plugins

import finance.tegro.rest.v2.dto.v1.ExchangePairDTOv1
import finance.tegro.rest.v2.exchangePairsFacade
import finance.tegro.rest.v2.services.MasterchainBlockService
import finance.tegro.rest.v2.services.PairV1CacheService
import finance.tegro.rest.v2.services.ReservesService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay

fun Application.configureRouting() = routing {
    route("/v1") {
        route("/pairs") {
            get {
                var result: List<ExchangePairDTOv1>?
                while (true) {
                    result = PairV1CacheService.getPairs()
                    if (result != null) {
                        call.respond(result)
                        break
                    } else {
                        delay(1000)
                    }
                }
            }
        }
    }
    route("/v2") {
        route("/masterchain-block") {
            get {
                call.respond(MasterchainBlockService.blockIdFlow.value.toString())
            }
        }
        route("/pairs") {
            get {
                call.respond(exchangePairsFacade.allExchangePairs())
            }
        }
        route("/reserves") {
            get {
                call.respond(ReservesService.reservesAll())
            }
        }
    }
}
