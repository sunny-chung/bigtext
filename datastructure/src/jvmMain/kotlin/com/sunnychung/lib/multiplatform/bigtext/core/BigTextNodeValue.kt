package com.sunnychung.lib.multiplatform.bigtext.core

import com.sunnychung.lib.multiplatform.bigtext.core.transform.BigTextTransformOffsetMapping
import com.sunnychung.lib.multiplatform.bigtext.util.findAllIndicesOfChar
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

    internal var node: RedBlackTree<BigTextNodeValue>.Node? = null

    private val key = RANDOM.nextInt()

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
        "$leftStringLength [$bufferIndex: $bufferOffsetStart ..< $bufferOffsetEndExclusive] L ${node.length()} r $leftNumOfRowBreaks/$rowBreakOffsets l $leftNumOfLineBreaks/$renderNumLineBreaksInRange lw $lastRowWidth $isEndWithForceRowBreak"

    protected fun CharSequence.quoteForMermaid(): String {
        return toString().replace("\n", "\\n").replace("\"", "&quot;")
    }

    companion object {
        private val RANDOM = Random(1000000)
    }
}

abstract class TextBuffer {
    private val mutableLineOffsetStarts = mutableListOf<Int>()

    /**
     * Line break positions in the domain of character indices of this buffer.
     */
    val lineOffsetStarts: List<Int> = mutableLineOffsetStarts
//    var lineOffsetStarts: SortedSet<Int> = sortedSetOf()
//    var rowOffsetStarts: List<Int> = emptyList()

    abstract val length: Int

    fun append(text: CharSequence): IntRange {
        log.v { "append ${text.length} start" }
        val start = length
        bufferAppend(text)
        log.v { "append ${text.length} after append" }
//        text.forEachIndexed { index, c ->
//            if (c == '\n') {
//                lineOffsetStarts += start + index
//            }
//        }
        text.findAllIndicesOfChar('\n').forEach {
            mutableLineOffsetStarts += start + it
        }
        log.v { "append ${text.length} end" }
        return start until start + text.length
    }

    abstract fun bufferAppend(text: CharSequence)

    override fun toString(): String {
        return subSequence(0, length).toString()
    }

    open fun subSequence(start: Int, endExclusive: Int): CharSequence {
        if (start >= endExclusive) {
            return ""
        }
        return bufferSubSequence(start, endExclusive)
    }

    open fun substring(start: Int, endExclusive: Int): String {
        if (start >= endExclusive) {
            return ""
        }
        return bufferSubstring(start, endExclusive)
    }

    abstract fun bufferSubstring(start: Int, endExclusive: Int): String

    abstract fun bufferSubSequence(start: Int, endExclusive: Int): CharSequence
}

enum class BufferOwnership {
    Owned, Delegated
}
