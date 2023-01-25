package finance.tegro.observer.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(prefix = "observer.block.id")
@ConstructorBinding
data class BlockIdServiceProperties(
    @DefaultValue("26208256")
    val genesisMasterchainSeqno: Int,
)
