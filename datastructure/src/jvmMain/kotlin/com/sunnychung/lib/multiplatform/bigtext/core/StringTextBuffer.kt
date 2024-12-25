package com.sunnychung.lib.multiplatform.bigtext.core

import java.nio.CharBuffer

class StringTextBuffer(size: Int) : TextBuffer(size) {
    private val buffer = StringBuilder(size)

    override val length: Int
        get() = buffer.length

    override fun bufferAppend(text: CharSequence) {
        buffer.append(text)
    }

    override fun bufferSubstring(start: Int, endExclusive: Int): String {
        return buffer.substring(start, endExclusive)
    }

    override fun bufferSubSequence(start: Int, endExclusive: Int): CharSequence {
//        return buffer.subSequence(start, endExclusive)
        return CharBuffer.wrap(buffer, start, endExclusive)
    }

    override fun get(index: Int): Char = buffer[index]
}
