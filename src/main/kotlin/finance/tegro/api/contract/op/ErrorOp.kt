package finance.tegro.api.contract.op

import org.ton.block.Maybe
import org.ton.block.MsgAddress
import org.ton.cell.Cell

sealed interface ErrorOp : Op {
    val queryId: ULong
    val destination: MsgAddress
    val payload: Maybe<Cell>
}
