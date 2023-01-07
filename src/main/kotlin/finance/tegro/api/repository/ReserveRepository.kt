package finance.tegro.api.repository

import finance.tegro.api.entity.Reserve
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ReserveRepository : JpaRepository<Reserve, UUID>
