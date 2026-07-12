package com.sunnychung.lib.multiplatform.bigtext.core.layout

import com.sunnychung.lib.multiplatform.bigtext.util.GraphemeClusters

private val LINE_BREAK_REGEX = "\n".toRegex()

class MonospaceTextLayouter<S> : TextLayouter {
    val charMeasurer: CharMeasurer<S>

    constructor(charMeasurer: CharMeasurer<S>) {
        this.charMeasurer = charMeasurer
    }

    override fun indexCharWidth(text: String) {
        charMeasurer.measureFullText(text)
    }

    override fun measureCharWidth(char: CharSequence): Float {
        return charMeasurer.findCharWidth(char)
    }

    override fun measureCharYOffset(char: CharSequence): Float {
        return charMeasurer.findCharYOffset(char)
    }

    override fun layoutOneLine(line: CharSequence, contentWidth: Float, firstRowOccupiedWidth: Float, offset: Int): Pair<List<Int>, Float> {
        // TODO optimize width measurement and row breaking
        val isOffsetLastLine = line.endsWith('\n')
        var numCharsPerRow = mutableListOf<Int>()
        var currentRowOccupiedWidth = firstRowOccupiedWidth
        var currentRowStartIndex = 0
        var previousClusterEndIndex = 0
        GraphemeClusters.forEach(line) { start, endExclusive -> // O(line string length)
            val w = charMeasurer.findCharWidth(line.subSequence(start, endExclusive))
            if (currentRowOccupiedWidth + w > contentWidth && (previousClusterEndIndex > currentRowStartIndex || currentRowOccupiedWidth > 0)) {
                numCharsPerRow += previousClusterEndIndex
                currentRowStartIndex = previousClusterEndIndex
                currentRowOccupiedWidth = 0f
            }
            currentRowOccupiedWidth += w
            previousClusterEndIndex = endExclusive
        }
//        if (numCharsInCurrentRow > 0) {
//            numCharsPerRow += numCharsInCurrentRow
//        }
//        if (numCharsPerRow.isEmpty()) {
//            numCharsPerRow += 0
//        }
        return numCharsPerRow.mapIndexed { index, it ->
            offset + it + if (index >= numCharsPerRow.lastIndex && isOffsetLastLine) 1 else 0 /* skip the last char '\n' */
        } to currentRowOccupiedWidth
    }
}

private infix fun Int.divRoundUp(other: Int): Int {
    val div = this / other
    val remainder = this % other
    return if (remainder == 0) {
        div
    } else {
        div + 1
    }
}
