package com.sunnychung.lib.multiplatform.bigtext.ux

interface BigTextManipulator {

    fun append(text: CharSequence)

    fun insertAt(pos: Int, text: CharSequence)

    fun replaceAtCursor(text: CharSequence)

    fun delete(range: IntRange)

    fun replace(range: IntRange, text: CharSequence)

    fun setCursorPosition(position: Int)

    fun setSelection(range: IntRange)
}
