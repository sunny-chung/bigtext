package com.sunnychung.lib.multiplatform.bigtext.core.layout

import com.sunnychung.lib.multiplatform.bigtext.annotation.TemporaryBigTextApi

interface CharMeasurer<S> {

    @Deprecated("Not maintained")
    fun measureFullText(text: CharSequence)

    fun findCharWidth(char: CharSequence, style: S? = null): Float

    fun findCharYOffset(char: CharSequence, style: S? = null): Float

    @TemporaryBigTextApi
    fun getRowHeight(): Float
}
