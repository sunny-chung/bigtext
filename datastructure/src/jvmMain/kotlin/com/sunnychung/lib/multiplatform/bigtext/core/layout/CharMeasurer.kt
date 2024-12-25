package com.sunnychung.lib.multiplatform.bigtext.core.layout

interface CharMeasurer<S> {

    @Deprecated("Not maintained")
    fun measureFullText(text: CharSequence)

    fun findCharWidth(char: CharSequence, style: S? = null): Float

    fun findCharYOffset(char: CharSequence, style: S? = null): Float
}
