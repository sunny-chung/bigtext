package com.sunnychung.application.multiplatform.hellohttp.ux.bigtext

import com.sunnychung.application.multiplatform.hellohttp.extension.binarySearchForMaxIndexOfValueAtMost
import com.sunnychung.application.multiplatform.hellohttp.util.CharMeasurer

private val LINE_BREAK_REGEX = "\n".toRegex()

class MonospaceTextLayouter : TextLayouter {
    val charMeasurer: CharMeasurer

    constructor(charMeasurer: CharMeasurer) {
        this.charMeasurer = charMeasurer
    }

    override fun indexCharWidth(text: String) {
        charMeasurer.measureFullText(text)
    }

    override fun layoutOneLine(line: CharSequence, contentWidth: Float, firstRowOccupiedWidth: Float, offset: Int): Pair<List<Int>, Float> {
        val charWidths = line.map { charMeasurer.findCharWidth(it.toString()) }
        val isOffsetLastLine = line.endsWith('\n')
        var numCharsPerRow = mutableListOf<Int>()
        var currentRowOccupiedWidth = firstRowOccupiedWidth
        var numCharsInCurrentRow = 0
        charWidths.forEachIndexed { i, w -> // O(line string length)
            if (currentRowOccupiedWidth + w > contentWidth && (numCharsInCurrentRow > 0 || currentRowOccupiedWidth > 0)) {
                numCharsPerRow += numCharsInCurrentRow
                numCharsInCurrentRow = 0
                currentRowOccupiedWidth = 0f
            }
            currentRowOccupiedWidth += w
            ++numCharsInCurrentRow
        }
//        if (numCharsInCurrentRow > 0) {
//            numCharsPerRow += numCharsInCurrentRow
//        }
//        if (numCharsPerRow.isEmpty()) {
//            numCharsPerRow += 0
//        }
        var s = 0
        return numCharsPerRow.mapIndexed { index, it ->
            s += it
            offset + s + if (index >= numCharsPerRow.lastIndex && isOffsetLastLine) 1 else 0 /* skip the last char '\n' */
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
