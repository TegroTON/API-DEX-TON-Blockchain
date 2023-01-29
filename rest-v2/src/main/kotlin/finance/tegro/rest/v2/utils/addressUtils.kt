package finance.tegro.rest.v2.utils

import org.ton.block.AddrStd
import org.ton.block.MsgAddress
import org.ton.lite.api.liteserver.LiteServerAccountId

fun MsgAddress.toAccountId() = when (this) {
    is AddrStd -> LiteServerAccountId(workchainId, address)
    else -> null
}
