package com.sunnychung.lib.multiplatform.bigtext.benchmark

import com.sunnychung.lib.multiplatform.kdatetime.KDuration
import com.sunnychung.lib.multiplatform.kdatetime.KInstant

fun measureTime(operation: () -> Unit): KDuration {
    val startInstant = KInstant.now()
    operation()
    val endInstant = KInstant.now()
    return endInstant - startInstant
}

fun measureAndPrint(label: String, operation: () -> Unit) {
    val duration = measureTime(operation)
    println("| $label | $duration |")
}
