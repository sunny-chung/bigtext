package com.sunnychung.lib.multiplatform.bigtext.test.util

import com.sunnychung.lib.multiplatform.bigtext.core.layout.CharMeasurer

class FixedWidthCharMeasurer(private val charWidth: Float) : CharMeasurer {
    override fun measureFullText(text: String) {
        // Nothing
    }

    override fun findCharWidth(char: String): Float = charWidth
}
