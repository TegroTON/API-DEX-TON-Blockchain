package finance.tegro.rest.dto

import org.ton.block.MsgAddress
import java.math.BigInteger
import java.time.Instant

data class ReferralDTO(
    val address: MsgAddress,
    val volumeTON: BigInteger,
    val invitedOn: Instant,
)
