package com.sunnychung.lib.multiplatform.bigtext.benchmark

import com.sunnychung.lib.multiplatform.bigtext.core.BigTextImpl
import com.sunnychung.lib.multiplatform.bigtext.core.layout.MonospaceTextLayouter
import com.sunnychung.lib.multiplatform.bigtext.core.transform.BigTextTransformerImpl
import com.sunnychung.lib.multiplatform.bigtext.test.random
import com.sunnychung.lib.multiplatform.bigtext.test.randomString
import com.sunnychung.lib.multiplatform.bigtext.test.util.FixedWidthCharMeasurer
import kotlin.random.Random
import kotlin.test.Test

class BigTextTransformationBenchmark {

    @Test
    fun transformReplace() {
        random = Random(10234567) // use a fixed seed for easier debug
        val t = BigTextImpl(chunkSize = 2 * 1024 * 1024).apply {
            val initial = randomString(10 * 1024 * 1024, isAddNewLine = true)
            append(initial)
            setLayouter(MonospaceTextLayouter(FixedWidthCharMeasurer(16f)))
            setSoftWrapEnabled(false)
        }
        val tt = BigTextTransformerImpl(t)
        val numTimes = 40000
        assert(t.length / numTimes >= 20)
        measureAndPrint("transformReplace $numTimes times") {
            tt.disableComputations()
            repeat(numTimes) { i ->
                val pos = t.length / numTimes * i
                tt.replace(pos ..< pos + 18, "<transformed>")
            }
            tt.enableAndDoComputations()
        }
    }
}
