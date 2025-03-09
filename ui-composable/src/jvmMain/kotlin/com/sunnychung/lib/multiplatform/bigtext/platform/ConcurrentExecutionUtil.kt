package com.sunnychung.lib.multiplatform.bigtext.platform

import com.sunnychung.lib.multiplatform.bigtext.util.AsyncContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask
import javax.swing.SwingUtilities

fun runAsync(operation: () -> Unit) {
    Thread {
        operation()
    }.start()
}

fun <R> runOnUiThreadAndReturnResult(operation: () -> R): R {
    return if (isOnUiThread()) {
        operation()
    } else {
        val task: FutureTask<R> = FutureTask(operation)
        SwingUtilities.invokeAndWait(task)
        try {
            task.get()
        } catch (e: ExecutionException) {
            throw e.cause!!
        }
    }
}

fun isOnUiThread(): Boolean {
    return SwingUtilities.isEventDispatchThread()
}

fun runAsyncOnUiThread(coroutineScope: CoroutineScope, operation: () -> Unit) {
    coroutineScope.launch {
        operation()
    }
}

object AsyncOperation {
    val Synchronous: AsyncContext.(computation: AsyncContext.() -> Unit) -> Unit = { it() }
    val Asynchronous: AsyncContext.(computation: AsyncContext.() -> Unit) -> Unit = { async { it() } }
}
