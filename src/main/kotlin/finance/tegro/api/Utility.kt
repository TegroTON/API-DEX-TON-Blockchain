package finance.tegro.api

import org.ton.block.*
import org.ton.boc.BagOfCells
import org.ton.cell.CellBuilder
import org.ton.crypto.decodeHex
import org.ton.crypto.encodeHex
import org.ton.lite.api.liteserver.LiteServerTransactionInfo
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb

fun LiteServerTransactionInfo.loadTransaction(): Transaction =
    BagOfCells(this.transaction).roots.first().parse { loadTlb(Transaction) }

fun MsgAddress.toSafeString() = when (this) {
    is AddrNone -> null
    is AddrStd -> this.toString(userFriendly = true, urlSafe = true, bounceable = true, testOnly = false)
    else -> BagOfCells(CellBuilder.createCell { storeTlb(MsgAddress, this@toSafeString) }).toByteArray().encodeHex()
}

fun String?.toMsgAddress(): MsgAddress {
    if (this == null) return AddrNone
    if (this.lowercase().trim() == "null" || this.lowercase().trim() == "none") return AddrNone

    return try {
        MsgAddressInt(this) // Try parsing simple address
    } catch (_: Exception) {
        BagOfCells(this.decodeHex()).roots.first().parse { loadTlb(MsgAddress) } // Parse raw boc
    }
}
