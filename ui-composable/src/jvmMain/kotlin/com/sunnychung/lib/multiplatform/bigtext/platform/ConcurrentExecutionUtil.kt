package com.sunnychung.lib.multiplatform.bigtext.platform

import com.sunnychung.lib.multiplatform.bigtext.util.AsyncContext

fun runAsync(operation: () -> Unit) {
    Thread {
        operation()
    }.start()
}

object AsyncOperation {
    val Synchronous: AsyncContext.(computation: AsyncContext.() -> Unit) -> Unit = { it() }
    val Asynchronous: AsyncContext.(computation: AsyncContext.() -> Unit) -> Unit = { async { it() } }
}
