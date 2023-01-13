package finance.tegro.rest.dto

import org.ton.block.MsgAddress
import java.math.BigInteger
import java.time.Instant

data class ReserveDTO(
    val address: MsgAddress,
    val base: BigInteger,
    val quote: BigInteger,
    val timestamp: Instant,
) {
    fun inverse() = copy(
        base = quote,
        quote = base,
    )
}
