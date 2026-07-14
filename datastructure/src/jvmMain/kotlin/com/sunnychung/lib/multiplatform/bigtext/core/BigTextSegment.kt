package com.sunnychung.lib.multiplatform.bigtext.core

internal data class BigTextSegment(
    val buffer: TextBuffer,
    val start: Int,
    val endExclusive: Int,
) {
    val length: Int
        get() = endExclusive - start

    operator fun get(index: Int): Char = buffer[start + index]

    fun slice(startIndex: Int, endIndex: Int): BigTextSegment =
        BigTextSegment(
            buffer = buffer,
            start = start + startIndex,
            endExclusive = start + endIndex,
        )
}
