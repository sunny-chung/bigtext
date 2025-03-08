package com.sunnychung.lib.multiplatform.bigtext.util

import com.sunnychung.lib.multiplatform.bigtext.platform.runAsync

interface AsyncContext {

    fun async(operation: () -> Unit) {
        runAsync(operation)
    }

    fun runOnUiDispatcher(operation: () -> Unit)
}
