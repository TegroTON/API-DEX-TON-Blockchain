package finance.tegro.api.repository

import finance.tegro.api.entity.Swap
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SwapRepository : JpaRepository<Swap, UUID> {
}
