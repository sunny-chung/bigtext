package com.sunnychung.lib.multiplatform.bigtext.core.transform

import com.sunnychung.lib.multiplatform.bigtext.core.BigTextNodeValue
import com.sunnychung.lib.multiplatform.bigtext.core.BufferOwnership
import com.sunnychung.lib.multiplatform.bigtext.core.renderLength
import com.williamfiset.algorithms.datastructures.balancedtree.RedBlackTree

/**
 *
 *                       o r i g i n a l T R A N S F O R M # # # t e x t
 *                       |             | |               | |   | |     |
 * leftStringLength      +*************+ |               | +***+*+*****+
 * leftTransformedLength |             | +***************+ |---| |     |
 * leftRenderLength      +*************+*+***************+ |   | +*****+
 * leftOverallLength     +*************+*+***************+*+***+*+*****+
 * 
 * # is a deleted character.
 */
class BigTextTransformNodeValue : BigTextNodeValue() {
    var transformedBufferStart: Int = -1
    var transformedBufferEndExclusive: Int = -1

    override var transformOffsetMapping: BigTextTransformOffsetMapping = BigTextTransformOffsetMapping.WholeBlock
    var incrementalTransformOffsetMappingLength = 0

    override val renderBufferStart: Int
        get() = if (bufferOwnership == BufferOwnership.Delegated) {
            bufferOffsetStart
        } else {
            transformedBufferStart
        }

    override val renderBufferEndExclusive: Int
        get() = if (bufferOwnership == BufferOwnership.Delegated) {
            bufferOffsetEndExclusive
        } else {
            transformedBufferEndExclusive
        }

    var leftTransformedLength: Int = 0
    val currentTransformedLength: Int
        get() = transformedBufferEndExclusive - transformedBufferStart - bufferLength

    override var leftRenderLength: Int = 0
    override val currentRenderLength: Int
        get() = if (bufferOwnership == BufferOwnership.Delegated) {
            bufferLength
        } else {
            transformedBufferEndExclusive - transformedBufferStart
        }

    override var leftOverallLength: Int = 0
    override val currentOverallLength: Int
        get() = bufferLength + currentRenderLength

    override fun debugLabel(node: RedBlackTree<BigTextNodeValue>.Node): String = buildString {
        node as RedBlackTree<BigTextTransformNodeValue>.Node

        append("$leftStringLength ${bufferOwnership.name.first()} blen=$bufferLength [$bufferIndex: $bufferOffsetStart ..< $bufferOffsetEndExclusive] L ${node.renderLength()}")
        append(" Tr [$transformedBufferStart ..< $transformedBufferEndExclusive]")
        append(" Ren left=$leftRenderLength curr=$currentRenderLength [$renderBufferStart ..< $renderBufferEndExclusive]")
        if (renderBufferStart in 0 until renderBufferEndExclusive) {
            append(" '${buffer.subSequence(renderBufferStart, renderBufferEndExclusive).quoteForMermaid()}'")
        }
        append(" M $incrementalTransformOffsetMappingLength")
        append(" row $leftNumOfRowBreaks/$rowBreakOffsets lw $lastRowWidth $isEndWithForceRowBreak")
        append(" line w end=$endLineWidth start=$startLineWidth mid=$middleMaxLineWidth aggEnd=$aggregatedEndLineWidth aggSt=$aggregatedStartLineWidth max=$maxLineWidth")
    }

}
