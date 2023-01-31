package finance.tegro.rest.v2.plugins

import finance.tegro.rest.v2.services.PairV1CacheService
import finance.tegro.rest.v2.services.PairsService
import finance.tegro.rest.v2.services.ReservesService
import finance.tegro.tonindexer.services.MasterchainBlockService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() = routing {
    get {
        call.respond("OK")
    }
    route("/v1") {
        route("/pairs") {
            get {
                call.respond(PairV1CacheService.getPairs())
            }
        }
        route("/pair") {
            get {
                call.respond(PairV1CacheService.getPairs())
            }
        }
    }
    route("/v2") {
        route("/masterchain-block") {
            get {
                call.respond(MasterchainBlockService.blockIdFlow.value.toString())
            }
        }
        route("/reserves") {
            get {
                call.respond(ReservesService.reservesAll())
            }
        }
        route("/pairs") {
            get {
                call.respond(PairsService.pairsAll())
            }
        }
    }
}
