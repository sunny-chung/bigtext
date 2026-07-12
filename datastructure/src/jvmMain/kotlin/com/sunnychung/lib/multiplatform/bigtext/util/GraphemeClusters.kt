package com.sunnychung.lib.multiplatform.bigtext.util

import java.text.BreakIterator
import java.util.Locale

object GraphemeClusters {
    inline fun forEach(text: CharSequence, action: (start: Int, endExclusive: Int) -> Unit) {
        if (text.isEmpty()) return

        if (!mayContainGraphemeSequence(text)) {
            var index = 0
            while (index < text.length) {
                val next = index + Character.charCount(Character.codePointAt(text, index))
                action(index, next)
                index = next
            }
            return
        }

        val iterator = BreakIterator.getCharacterInstance(Locale.ROOT)
        iterator.setText(text.toString())
        var start = iterator.first()
        var end = iterator.next()
        while (end != BreakIterator.DONE) {
            action(start, end)
            start = end
            end = iterator.next()
        }
    }

    fun boundaryAtOrBefore(text: CharSequence, index: Int): Int {
        val boundedIndex = index.coerceIn(0, text.length)
        if (boundedIndex == 0 || boundedIndex == text.length) return boundedIndex

        if (!mayContainGraphemeSequence(text)) {
            return if (text[boundedIndex].isLowSurrogate() && text[boundedIndex - 1].isHighSurrogate()) {
                boundedIndex - 1
            } else {
                boundedIndex
            }
        }

        var result = 0
        forEach(text) { _, endExclusive ->
            if (endExclusive <= boundedIndex) {
                result = endExclusive
            } else {
                return result
            }
        }
        return result
    }

    fun boundaryAtOrAfter(text: CharSequence, index: Int): Int {
        val boundedIndex = index.coerceIn(0, text.length)
        if (boundedIndex == 0 || boundedIndex == text.length) return boundedIndex

        if (!mayContainGraphemeSequence(text)) {
            return if (text[boundedIndex].isLowSurrogate() && text[boundedIndex - 1].isHighSurrogate()) {
                boundedIndex + 1
            } else {
                boundedIndex
            }
        }

        forEach(text) { start, endExclusive ->
            if (boundedIndex == start) return start
            if (boundedIndex in (start + 1)..<endExclusive) return endExclusive
        }
        return text.length
    }

    fun isBoundary(text: CharSequence, index: Int): Boolean =
        boundaryAtOrBefore(text, index) == index

    fun mayContainGraphemeSequence(text: CharSequence): Boolean {
        var index = 0
        while (index < text.length) {
            val codePoint = Character.codePointAt(text, index)
            if (isGraphemeSequenceCodePoint(codePoint)) {
                return true
            }
            index += Character.charCount(codePoint)
        }
        return false
    }

    private fun isGraphemeSequenceCodePoint(codePoint: Int): Boolean = when {
        codePoint == 0x200D -> true // zero-width joiner
        codePoint in 0x0300..0x036F -> true // combining diacritical marks
        codePoint in 0x1AB0..0x1AFF -> true
        codePoint in 0x1DC0..0x1DFF -> true
        codePoint in 0x20D0..0x20FF -> true
        codePoint in 0xFE20..0xFE2F -> true
        codePoint in 0xFE00..0xFE0F -> true // variation selectors
        codePoint in 0xE0100..0xE01EF -> true
        codePoint in 0x1F1E6..0x1F1FF -> true // regional indicators
        codePoint in 0x1F3FB..0x1F3FF -> true // emoji modifiers
        codePoint in 0xE0020..0xE007F -> true // emoji tag sequences
        codePoint == 0x20E3 -> true // keycap combining mark
        else -> false
    }
}
