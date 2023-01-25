package finance.tegro.rest.controller

import finance.tegro.core.repository.ExchangePairRepository
import finance.tegro.core.repository.SwapRepository
import finance.tegro.core.toMsgAddress
import finance.tegro.core.toSafeString
import finance.tegro.rest.dto.ReferralDTO
import mu.KLogging
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.ton.block.MsgAddress
import java.math.RoundingMode
import javax.persistence.EntityManager

@RestController
@RequestMapping("/v1/referral")
class ReferralController(
    private val entityManager: EntityManager,
    private val exchangePairRepository: ExchangePairRepository,
    private val swapRepository: SwapRepository,
) {
    @Cacheable("referrals")
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

    @CacheEvict("referrals", allEntries = true)
    @Scheduled(fixedRateString = "\${observer.cache.referrals:10000}")
    fun evictAllReferrals() {
        logger.debug { "Evicting all referrals" }
    }

    @Cacheable("referral")
    @GetMapping("/{referrer}/{referral}")
    fun getSpecificReferral(@PathVariable referrer: MsgAddress, @PathVariable referral: MsgAddress): ReferralDTO {
        val tegroExchangePair =
            exchangePairRepository.findByAddress("EQDTjWuJmwD5SJ8l8L0zoNy8mJP4aJ_k6b4Eg2vm88lCpAIC".toMsgAddress())
                .orElseThrow { IllegalStateException("Tegro exchange pair not found") }
        val firstSwap = swapRepository.findFirstByDestinationOrderByBlockId_TimestampAsc(referral)
            .orElseThrow { throw IllegalArgumentException("Referral ${referral.toSafeString()} of ${referrer.toSafeString()} not found") }
        val volume = SwapRepository.findAccountVolume(entityManager, referral)
            .map { (time, volume) ->
                val price = swapRepository.findPriceOn(tegroExchangePair, time)
                    .orElseThrow { IllegalStateException("Failed to get price of TGR for $time") }

                volume.toBigDecimal(tegroExchangePair.token?.baseToken?.metadata?.decimals ?: 0)
                    .multiply(price)
                    .setScale(tegroExchangePair.token?.quoteToken?.metadata?.decimals ?: 0, RoundingMode.HALF_DOWN)
            }
            .sumOf { it }

        return ReferralDTO(
            referral,
            volume,
            firstSwap.blockId.timestamp,
        )
    }

    @CacheEvict("referral", allEntries = true)
    @Scheduled(fixedRateString = "\${observer.cache.referral:10000}")
    fun evictAllReferral() {
        logger.debug { "Evicting all referral" }
    }

    companion object : KLogging()
}
