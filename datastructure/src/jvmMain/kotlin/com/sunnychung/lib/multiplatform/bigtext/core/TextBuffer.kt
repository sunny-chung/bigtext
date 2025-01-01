package com.sunnychung.lib.multiplatform.bigtext.core

import com.sunnychung.lib.multiplatform.bigtext.util.IntList
import com.sunnychung.lib.multiplatform.bigtext.util.findAllIndicesOfChar

abstract class TextBuffer(val size: Int): CharSequence {
    private val mutableLineOffsetStarts = IntList()

    /**
     * Line break positions in the domain of character indices of this buffer.
     */
    val lineOffsetStarts: IntList = mutableLineOffsetStarts
//    var lineOffsetStarts: SortedSet<Int> = sortedSetOf()
//    var rowOffsetStarts: List<Int> = emptyList()

    abstract override val length: Int

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

    override open fun subSequence(start: Int, endExclusive: Int): CharSequence {
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

    abstract override operator fun get(index: Int): Char
}
