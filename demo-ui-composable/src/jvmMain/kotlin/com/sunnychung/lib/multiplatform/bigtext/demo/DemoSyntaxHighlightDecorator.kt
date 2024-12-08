package com.sunnychung.lib.multiplatform.bigtext.demo

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import com.sunnychung.lib.multiplatform.bigtext.core.BigText
import com.sunnychung.lib.multiplatform.bigtext.core.BigTextChangeEvent
import com.sunnychung.lib.multiplatform.bigtext.core.CacheableBigTextDecorator
import com.sunnychung.lib.multiplatform.bigtext.extension.binarySearchForMaxIndexOfValueAtMost
import com.sunnychung.lib.multiplatform.bigtext.extension.binarySearchForMinIndexOfValueAtLeast
import com.sunnychung.lib.multiplatform.bigtext.extension.length
import com.sunnychung.lib.multiplatform.bigtext.util.string

private val NUMBER_PATTERN = "\\b([0-9]+)\\b".toRegex()
private val WORD_PATTERN = "\\b([a-zA-Z]+)\\b".toRegex()

private val NUMBER_STYLE = SpanStyle(Color(red = 0.1f, green = 0f, blue = 0.55f))
private val WORD_STYLE = SpanStyle(Color(red = 0.1f, green = 0.4f, blue = 0.1f))

/**
 * This demo syntax highlighter is SLOW. You can make it works with 100+ MB by
 * changing it to incremental, or parsing in background and not reacting to every keystroke.
 */
class DemoSyntaxHighlightDecorator : CacheableBigTextDecorator() {
    private var highlightedStyles: List<AnnotatedString.Range<SpanStyle>> = emptyList()

    override fun doInitialize(text: BigText) {
        highlight(text)
    }

    override fun afterTextChange(change: BigTextChangeEvent) {
        highlight(change.bigText)
    }

    private fun highlight(bigText: BigText) { // Time Complexity: O(max(L, M log2 M)), L = length, M = no. of matches
        val text = bigText.buildString()
        val styles = mutableListOf<AnnotatedString.Range<SpanStyle>>()
        listOf(NUMBER_PATTERN to NUMBER_STYLE, WORD_PATTERN to WORD_STYLE).forEach { (pattern, style) ->
            pattern.findAll(text).forEach { matches ->
                val match = matches.groups[1]!!
                val range = match.range
                styles.add(AnnotatedString.Range(style, range.start, range.endInclusive + 1))
            }
        }
        highlightedStyles = styles.sortedBy { it.start }
    }

    override fun onApplyDecorationOnOriginal(text: CharSequence, originalRange: IntRange): CharSequence {
        val startIndex = binarySearchForMinIndexOfValueAtLeast(highlightedStyles.indices, originalRange.start) { highlightedStyles[it].end }
        val endIndex = binarySearchForMaxIndexOfValueAtMost(highlightedStyles.indices, originalRange.endInclusive) { highlightedStyles[it].start }
            .coerceAtMost(highlightedStyles.lastIndex)
        val highlightedStylesSubList = highlightedStyles.subList(startIndex, endIndex + 1)
            .map {
                it.copy(
                    start = maxOf(0, it.start - originalRange.start),
                    end = minOf(originalRange.length, it.end - originalRange.start)
                )
            }
        return AnnotatedString(text.string(), highlightedStylesSubList)
    }
}
