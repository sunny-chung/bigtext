package com.sunnychung.lib.multiplatform.bigtext.compose

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import com.lodborg.intervaltree.IntegerInterval
import com.lodborg.intervaltree.Interval
import com.lodborg.intervaltree.IntervalTree
import com.sunnychung.lib.multiplatform.bigtext.core.TextBuffer

class AnnotatedStringTextBuffer(size: Int) : TextBuffer(size) {
    private val buffer = StringBuilder(size)

    private val spanStyles = IntervalTree<Int>()

    override val length: Int
        get() = buffer.length

    override fun bufferAppend(text: CharSequence) {
        if (text is AnnotatedString) {
            val baseStart = buffer.length
            text.spanStyles.forEach {
                val start = it.start + baseStart
                val endExclusive = it.end + baseStart
                spanStyles.add(Entry(start until endExclusive, it.item, it.tag))
            }
            buffer.append(text)
            return
        }
        buffer.append(text)
    }

    override fun bufferSubstring(start: Int, endExclusive: Int): String {
        return buffer.substring(start, endExclusive)
    }

    override fun bufferSubSequence(start: Int, endExclusive: Int): CharSequence {
        return AnnotatedString(
            text = buffer.substring(start, endExclusive),
            spanStyles = spanStyles.query(IntegerInterval(start, endExclusive - 1, Interval.Bounded.CLOSED))
                .map { item ->
                    val it = item as Entry
                    AnnotatedString.Range(
                        item = it.style,
                        start = maxOf(0, it.range.start - start),
                        end = minOf(endExclusive - start, it.range.endInclusive + 1 - start),
                        tag = it.tag
                    )
                },
            paragraphStyles = emptyList()
        )
    }

    override fun get(index: Int): Char = buffer[index]

    private class Entry(val range: IntRange, val style: SpanStyle, val tag: String) : IntegerInterval(range.start, range.endInclusive, Bounded.CLOSED)
}
