package com.sunnychung.lib.multiplatform.bigtext.util

import com.sunnychung.lib.multiplatform.bigtext.platform.runAsync
import com.sunnychung.lib.multiplatform.bigtext.platform.runAsyncOnUiThread
import com.sunnychung.lib.multiplatform.bigtext.platform.runOnUiThreadAndReturnResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive

open class AsyncContext(private val coroutineScope: CoroutineScope) {

    fun async(operation: () -> Unit) {
        runAsync(operation)
    }

    fun asyncOnUiDispatcher(operation: () -> Unit) {
        if (coroutineScope.isActive) {
            runAsyncOnUiThread(coroutineScope, operation)
        }
    }

    fun <R> returnFromUiDispatcher(operation: () -> R): R? {
        return if (coroutineScope.isActive) {
            runOnUiThreadAndReturnResult(operation)
        } else {
            null
        }
    }
}
