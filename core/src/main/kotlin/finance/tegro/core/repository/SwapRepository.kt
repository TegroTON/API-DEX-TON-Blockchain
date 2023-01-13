package finance.tegro.core.repository;

import finance.tegro.core.entity.Swap
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface SwapRepository : JpaRepository<Swap, UUID> {
}
