package finance.tegro.rest.controller

import finance.tegro.core.repository.SwapRepository
import finance.tegro.core.toSafeString
import finance.tegro.rest.dto.ReferralDTO
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.ton.block.MsgAddress
import java.math.BigInteger

@RestController
@RequestMapping("/v1/referral")
class ReferralController(
    private val swapRepository: SwapRepository,
) {
    @GetMapping("/{referrer}")
    fun getAllReferrals(@PathVariable referrer: MsgAddress): List<ReferralDTO> =
        swapRepository.findAllReferrals(referrer)
            .mapNotNull { referral ->
                try {
                    getSpecificReferral(referrer, referral)
                } catch (_: Exception) {
                    null
                }
            }

    @GetMapping("/{referrer}/{referral}")
    fun getSpecificReferral(@PathVariable referrer: MsgAddress, @PathVariable referral: MsgAddress): ReferralDTO {
        val firstSwap = swapRepository.findFirstReferralSwap(referrer, referral)
            .orElseThrow { throw IllegalArgumentException("Referral ${referral.toSafeString()} of ${referrer.toSafeString()} not found") }
        val volumeTON = swapRepository.findAccountVolumeTON(referral, firstSwap.blockId.timestamp)
            .orElse(BigInteger.ZERO)

        return ReferralDTO(
            referral,
            volumeTON,
            firstSwap.blockId.timestamp,
        )
    }
}
