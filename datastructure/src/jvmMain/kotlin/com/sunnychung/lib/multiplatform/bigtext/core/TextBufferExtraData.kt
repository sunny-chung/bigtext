package com.sunnychung.lib.multiplatform.bigtext.core

class TextBufferExtraData(val size: Int) {
    /**
     * Multiplied by 10. e.g. width 1.23f is represented as 12.
     *
     * Performance of using Array VS List: 1.7s VS 4.4s
     */
//    val widths: MutableList<Long> = MutableList(size) { 0L }
    val widths = LongArray(size)

    var hasInitialized: Boolean = false
}
