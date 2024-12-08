package com.sunnychung.lib.multiplatform.bigtext.extension

fun String.insert(pos: Int, insert: String): String {
    var s = ""
    if (pos > 0) {
        s += substring(0 until pos)
    }
    s += insert
    if (pos < length) {
        s += substring(pos)
    }
    return s
}

fun String.endWithNewLine() =
    if (endsWith('\n')) {
        this
    } else {
        this + '\n'
    }

fun String?.countNotBlank(): Int = if (isNullOrBlank()) 0 else 1
