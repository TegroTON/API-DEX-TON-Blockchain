package finance.tegro.rest.v2.plugins

import finance.tegro.rest.v2.dto.v1.ExchangePairDTOv1
import finance.tegro.rest.v2.dto.v1.ReserveDTOv1
import finance.tegro.rest.v2.services.MasterchainBlockService
import finance.tegro.rest.v2.services.PairV1CacheService
import finance.tegro.rest.v2.services.PairsService
import finance.tegro.rest.v2.services.ReservesService
import finance.tegro.rest.v2.utils.toAccountId
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import org.ton.block.AddrStd

fun Application.configureRouting() = routing {
    get("favicon.ico") {
        call.respondRedirect("https://tegro.finance/assets/favicon.ico")
    }
    get {
        call.respondRedirect("/v2")
    }
    route("/v1") {
        route("/pairs") {
            get {
                var result: List<ExchangePairDTOv1>?
                while (true) {
                    result = PairV1CacheService.getPairs()
                    if (result != null) {
                        result = result.mapNotNull {
                            val accountId =
                                AddrStd.parseUserFriendly(it.address).toAccountId() ?: return@mapNotNull null
                            val reserves = ReservesService.reserves(accountId).value
                            it.copy(
                                reserve = ReserveDTOv1(
                                    address = it.address,
                                    base = reserves.base,
                                    quote = reserves.quote,
                                    timestamp = Clock.System.now()
                                )
                            )
                        }
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
        get {
            call.respond("OK")
        }
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
