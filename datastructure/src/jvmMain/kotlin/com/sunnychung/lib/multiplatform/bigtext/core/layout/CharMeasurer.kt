package com.sunnychung.lib.multiplatform.bigtext.core.layout

interface CharMeasurer {

    fun measureFullText(text: String)

    fun findCharWidth(char: String): Float
}
