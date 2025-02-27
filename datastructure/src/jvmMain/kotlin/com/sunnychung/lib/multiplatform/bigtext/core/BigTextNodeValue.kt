package com.sunnychung.lib.multiplatform.bigtext.core

import com.sunnychung.lib.multiplatform.bigtext.core.transform.BigTextTransformOffsetMapping
import com.williamfiset.algorithms.datastructures.balancedtree.RedBlackTree
import kotlin.random.Random

open class BigTextNodeValue : Comparable<BigTextNodeValue>, DebuggableNode<BigTextNodeValue>, LengthNodeValue {
    var leftNumOfLineBreaks: Int = -1
    var leftNumOfRowBreaks: Int = -1
    override var leftStringLength: Int = -1
//    var rowBreakOffsets: SortedSet<Int> = sortedSetOf()
    /**
     * Row break positions in the domain of character indices of the {bufferIndex}-th buffer.
     */
    var rowBreakOffsets: List<Int> = emptyList()
    var lastRowWidth: Float = 0f
    var isEndWithForceRowBreak: Boolean = false

    var bufferIndex: Int = -1
    var bufferOffsetStart: Int = -1
    var bufferOffsetEndExclusive: Int = -1
    @Deprecated("use renderNumLineBreaksInRange") var bufferNumLineBreaksInRange: Int = -1
    var renderNumLineBreaksInRange: Int = -1
    lateinit var buffer: TextBuffer
    var bufferOwnership: BufferOwnership = BufferOwnership.Owned

    override val bufferLength: Int
        get() = bufferOffsetEndExclusive - bufferOffsetStart

    override val leftOverallLength: Int
        get() = leftStringLength

    override val currentOverallLength: Int
        get() = bufferLength

    override val leftRenderLength: Int
        get() = leftStringLength

    override val currentRenderLength: Int
        get() = bufferLength

    open val renderBufferStart: Int
        get() = bufferOffsetStart

    open val renderBufferEndExclusive: Int
        get() = bufferOffsetEndExclusive

    override val transformOffsetMapping: BigTextTransformOffsetMapping
        get() = BigTextTransformOffsetMapping.WholeBlock

    /**
     * startLineWidth = known line width of beginning of current node, limited by current buffer
     *
     * If there is only one line, only `endLineWidth` has non-negative valid value.
     * If there are two lines, only `startLineWidth` and `endLineWidth` have non-negative valid values.
     * `middleMaxLineWidth` stores the maximum width excluding startLineWidth and endLineWidth.
     */
    var startLineWidth: Long = -1
    var middleMaxLineWidth: Long = -1
    var endLineWidth: Long = -1
    var aggregatedStartLineWidth: Long = 0
    var aggregatedEndLineWidth: Long = 0
    var aggregatedStartLineIdentifier: Any? = null
    var aggregatedEndLineIdentifier: Any? = null

    var maxLineWidth: Long = -1

    internal var node: RedBlackTree<BigTextNodeValue>.Node? = null

    private val key = RANDOM.nextInt()

    fun invalidateCacheProperties() {
        middleMaxLineWidth = -1
    }

    override fun attach(node: RedBlackTree<BigTextNodeValue>.Node) {
        this.node = node
    }

    override fun detach() {
        node = null
    }

    override fun compareTo(other: BigTextNodeValue): Int {
        return compareValues(leftOverallLength, other.leftOverallLength)
    }

    fun clone(node: RedBlackTree<BigTextNodeValue>.Node?): BigTextNodeValue {
        return BigTextNodeValue().also {
            it.leftNumOfLineBreaks = leftNumOfLineBreaks
            it.leftNumOfRowBreaks = leftNumOfRowBreaks
            it.leftStringLength = leftStringLength
            it.rowBreakOffsets = rowBreakOffsets.toList()
            it.lastRowWidth = lastRowWidth
            it.isEndWithForceRowBreak = isEndWithForceRowBreak
            it.bufferIndex = bufferIndex
            it.bufferOffsetStart = bufferOffsetStart
            it.bufferOffsetEndExclusive = bufferOffsetEndExclusive
            it.bufferNumLineBreaksInRange = bufferNumLineBreaksInRange
            it.renderNumLineBreaksInRange = renderNumLineBreaksInRange
            it.buffer = buffer // same ref
            it.bufferOwnership = bufferOwnership
            it.node = node
        }
    }

    override fun debugKey(): String = "$key"
    override fun debugLabel(node: RedBlackTree<BigTextNodeValue>.Node): String =
//        "$leftStringLength [$bufferIndex: $bufferOffsetStart ..< $bufferOffsetEndExclusive] L ${node.length()} r $leftNumOfRowBreaks/$rowBreakOffsets lw $lastRowWidth $isEndWithForceRowBreak '${buffer.subSequence(renderBufferStart, renderBufferEndExclusive).toString().replace("\n", "\\n")}'"
        "|${debugKey()}| $leftStringLength [$bufferIndex: $bufferOffsetStart ..< $bufferOffsetEndExclusive] L ${node.length()} r $leftNumOfRowBreaks/$rowBreakOffsets l $leftNumOfLineBreaks/$renderNumLineBreaksInRange lw $lastRowWidth $isEndWithForceRowBreak" +
                " '${buffer.substring(bufferOffsetStart, bufferOffsetEndExclusive)}' " +
                " line w end=$endLineWidth start=$startLineWidth mid=$middleMaxLineWidth aggEnd=$aggregatedEndLineWidth aggSt=$aggregatedStartLineWidth max=$maxLineWidth"

    protected fun CharSequence.quoteForMermaid(): String {
        return toString().replace("\n", "\\n").replace("\"", "&quot;")
    }

    companion object {
        private val RANDOM = Random(1000000)
    }
}

enum class BufferOwnership {
    Owned, Delegated
}
