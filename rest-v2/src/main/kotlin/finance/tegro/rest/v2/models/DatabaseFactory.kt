package finance.tegro.rest.v2.models

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object DatabaseFactory {
    fun init(
        url: String = System.getenv("DATABASE_URL"),
        username: String = System.getenv("DATABASE_USERNAME"),
        password: String = System.getenv("DATABASE_PASSWORD")
    ) {
        Database.connect(
            url = url,
            user = username,
            password = password
        )
    }
}


suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }
