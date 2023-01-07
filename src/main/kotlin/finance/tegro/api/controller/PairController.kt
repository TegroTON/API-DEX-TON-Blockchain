package finance.tegro.api.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/pair")
class PairController {
    @GetMapping("/")
    fun all() {

    }

    @GetMapping("/{address}")
    fun byAddress() {

    }

    @GetMapping("/find/address/{left}/{right}")
    fun byAddresses() {

    }

    @GetMapping("/find/symbol/{left}/{right}")
    fun bySymbols() {

    }
}
