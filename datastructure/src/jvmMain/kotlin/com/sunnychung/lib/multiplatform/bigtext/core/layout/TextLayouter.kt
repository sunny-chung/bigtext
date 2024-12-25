package com.sunnychung.lib.multiplatform.bigtext.core.layout

interface TextLayouter {

    fun indexCharWidth(text: String)

    fun measureCharWidth(char: CharSequence): Float

    fun measureCharYOffset(char: CharSequence): Float

    fun layoutOneLine(line: CharSequence, contentWidth: Float, firstRowOccupiedWidth: Float, offset: Int): Pair<List<Int>, Float>
}
