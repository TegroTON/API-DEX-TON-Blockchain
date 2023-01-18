package finance.tegro.rest.dto

import com.fasterxml.jackson.annotation.JsonProperty
import org.ton.block.MsgAddress
import java.math.BigDecimal
import java.time.Instant

data class ReferralDTO(
    @JsonProperty("address")
    val address: MsgAddress,
    @JsonProperty("volume_tgr")
    val volumeTGR: BigDecimal,
    @JsonProperty("invited")
    val invited: Instant,
)
