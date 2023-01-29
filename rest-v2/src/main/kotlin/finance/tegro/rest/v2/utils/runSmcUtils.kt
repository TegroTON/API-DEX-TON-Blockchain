package finance.tegro.rest.v2.utils

import org.ton.block.VmStack
import org.ton.block.VmStackList
import org.ton.block.VmStackValue
import org.ton.boc.BagOfCells
import org.ton.cell.CellBuilder
import org.ton.crypto.crc16
import org.ton.tlb.storeTlb

internal fun smcMethodId(methodName: String): Long = crc16(methodName).toLong() or 0x10000

internal fun smcCreateParams(
    vmStack: VmStack
): BagOfCells = BagOfCells(
    CellBuilder.createCell {
        storeTlb(VmStack, vmStack)
    }
)

internal fun smcCreateParams(
    params: Iterable<VmStackValue>
): BagOfCells = smcCreateParams(VmStack(VmStackList(params.asIterable())))
