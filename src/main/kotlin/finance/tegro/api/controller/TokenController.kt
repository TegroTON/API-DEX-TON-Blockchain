package finance.tegro.api.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/token")
class TokenController {
    @GetMapping("/")
    fun all() {

    }

    @GetMapping("/address/{address}")
    fun byAddress() {

    }

    @GetMapping("/symbol/{symbol}")
    fun bySymbol() {

    }

    @GetMapping("/address/{address}/image")
    fun image() {

    }
}
