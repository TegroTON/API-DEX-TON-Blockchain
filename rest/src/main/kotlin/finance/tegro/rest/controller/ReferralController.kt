package finance.tegro.rest.controller

import finance.tegro.core.repository.SwapRepository
import finance.tegro.core.toSafeString
import finance.tegro.rest.dto.ReferralDTO
import mu.KLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.ton.block.MsgAddress
import java.math.BigInteger
import javax.persistence.EntityManager

@RestController
@RequestMapping("/v1/referral")
class ReferralController(
    private val entityManager: EntityManager,
    private val swapRepository: SwapRepository,
) {
    @GetMapping("/{referrer}")
    fun getAllReferrals(@PathVariable referrer: MsgAddress): List<ReferralDTO> =
        SwapRepository.findAllReferrals(entityManager, referrer)
            .mapNotNull { referral ->
                try {
                    getSpecificReferral(referrer, referral)
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to get referral for ${referrer.toSafeString()}" }
                    null
                }
            }

    @GetMapping("/{referrer}/{referral}")
    fun getSpecificReferral(@PathVariable referrer: MsgAddress, @PathVariable referral: MsgAddress): ReferralDTO {
        val firstSwap = swapRepository.findFirstByDestinationOrderByBlockId_TimestampAsc(referral)
            .orElseThrow { throw IllegalArgumentException("Referral ${referral.toSafeString()} of ${referrer.toSafeString()} not found") }
        val volumeTON = SwapRepository.findAccountVolumeTON(entityManager, referral) ?: BigInteger.ZERO

        return ReferralDTO(
            referral,
            volumeTON,
            firstSwap.blockId.timestamp,
        )
    }

    companion object : KLogging()
}
