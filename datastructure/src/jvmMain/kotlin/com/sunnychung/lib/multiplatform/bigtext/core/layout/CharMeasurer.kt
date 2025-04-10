package com.sunnychung.lib.multiplatform.bigtext.core.layout

import com.sunnychung.lib.multiplatform.bigtext.annotation.TemporaryApi

interface CharMeasurer<S> {

    @Deprecated("Not maintained")
    fun measureFullText(text: CharSequence)

    fun findCharWidth(char: CharSequence, style: S? = null): Float

    fun findCharYOffset(char: CharSequence, style: S? = null): Float

    @TemporaryApi
    fun getRowHeight(): Float
}
