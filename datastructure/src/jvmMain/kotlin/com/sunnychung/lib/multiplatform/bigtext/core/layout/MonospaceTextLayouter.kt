package com.sunnychung.lib.multiplatform.bigtext.core.layout

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
        var surrogatePairFirstChar: Char? = null
        val charWidths = line.mapIndexed { index, it ->
//            if (it.isHighSurrogate()) {
//                surrogatePairFirstChar = it
//                return@map 0f
//            }
//            val char = if (surrogatePairFirstChar == null) {
//                it.toString()
//            } else {
//                "$surrogatePairFirstChar$it".also {
//                    surrogatePairFirstChar = null
//                }
//            }
            val char = if (it.isHighSurrogate()) { // let the first char to expand width, otherwise the surrogate pair may be broken into two half
//                "$it${line[index + 1]}"
                if (index + 1 <= line.lastIndex) {
                    line.subSequence(index, index + 2)
                } else { // TODO if low surrogate is not in this buffer
                    line.subSequence(index, index + 1)
                }
            } else if (it.isLowSurrogate()) {
                return@mapIndexed 0f
            } else {
//                it.toString()
                line.subSequence(index, index + 1)
            }
            charMeasurer.findCharWidth(char)
        }
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
