package finance.tegro.tonindexer.ydb

import kotlinx.coroutines.future.await
import tech.ydb.auth.iam.CloudAuthHelper
import tech.ydb.core.Result
import tech.ydb.core.grpc.GrpcTransport
import tech.ydb.table.Session
import tech.ydb.table.SessionRetryContext
import tech.ydb.table.TableClient
import tech.ydb.table.description.TableDescription
import tech.ydb.table.query.DataQueryResult
import tech.ydb.table.query.Params
import tech.ydb.table.transaction.TxControl
import java.time.Duration
import java.util.concurrent.CompletableFuture

object Ydb {
    private var retryCtx: SessionRetryContext? = null
    lateinit var database: String

    // https://cloud.yandex.ru/docs/ydb/operations/connection
    fun init(
        endpoint: String = "ydb.serverless.yandexcloud.net:2135",
        database: String = "/ru-central1/b1gm00sspsqovaqbkmhr/etnlc55k4s8jkcpodlo7"
    ) {
        this.database = database
        val authProvider = CloudAuthHelper.getAuthProviderFromEnviron()

        val transport = GrpcTransport.forEndpoint(endpoint, database)
            .withAuthProvider(authProvider)
            .withSecureConnection()
            .build()

        val tableClient = TableClient.newClient(transport).build()
        require(tableClient.createSession(Duration.ofSeconds(10)).join().isSuccess)
        retryCtx = SessionRetryContext.create(tableClient).build()
    }

    suspend fun dataQuery(query: String, params: Params = Params.empty()): DataQueryResult {
        val txControl = TxControl.serializableRw().setCommitTx(true)
        return session {
            executeDataQuery(query, txControl, params)
        }
    }

    suspend fun describeTable(tableName: String): TableDescription {
        return session {
            describeTable("$database/$tableName")
        }
    }

    suspend fun <T> session(session: Session.() -> CompletableFuture<Result<T>>): T {
        val retryCtx = requireNotNull(retryCtx)
        return retryCtx.supplyResult {
            session(it)
        }.await().value
    }
}
