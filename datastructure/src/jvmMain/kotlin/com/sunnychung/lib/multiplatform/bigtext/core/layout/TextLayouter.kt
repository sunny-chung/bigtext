package com.sunnychung.lib.multiplatform.bigtext.core.layout

interface TextLayouter {

    fun indexCharWidth(text: String)

    fun measureCharWidth(char: String): Float

    fun layoutOneLine(line: CharSequence, contentWidth: Float, firstRowOccupiedWidth: Float, offset: Int): Pair<List<Int>, Float>
}
