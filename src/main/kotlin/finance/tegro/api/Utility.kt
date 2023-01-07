package finance.tegro.api

import org.ton.block.Transaction
import org.ton.boc.BagOfCells
import org.ton.lite.api.liteserver.LiteServerTransactionInfo
import org.ton.tlb.loadTlb

fun LiteServerTransactionInfo.loadTransaction(): Transaction =
    BagOfCells(this.transaction).roots.first().parse { loadTlb(Transaction) }
