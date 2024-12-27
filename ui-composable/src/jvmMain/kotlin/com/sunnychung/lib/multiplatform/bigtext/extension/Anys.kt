package com.sunnychung.lib.multiplatform.bigtext.extension

fun <T> T.runIf(condition: Boolean, block: T.() -> T) : T {
    return if (condition) {
        block()
    } else {
        this
    }
}
