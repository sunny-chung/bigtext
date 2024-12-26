package com.sunnychung.lib.multiplatform.bigtext.demo.benchmark

import androidx.compose.runtime.getValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import com.sunnychung.lib.multiplatform.bigtext.compose.AnnotatedStringTextBuffer
import com.sunnychung.lib.multiplatform.bigtext.compose.ComposeUnicodeCharMeasurer
import com.sunnychung.lib.multiplatform.bigtext.core.BigTextImpl
import com.sunnychung.lib.multiplatform.bigtext.core.layout.MonospaceTextLayouter
import com.sunnychung.lib.multiplatform.bigtext.core.transform.BigTextTransformerImpl
import com.sunnychung.lib.multiplatform.bigtext.core.transform.ConcurrentBigTextTransformed
import com.sunnychung.lib.multiplatform.bigtext.demo.FixedWidthCharMeasurer
import com.sunnychung.lib.multiplatform.bigtext.demo.TRANSFORMATION_PRELOAD_CONTENT
import com.sunnychung.lib.multiplatform.bigtext.demo.VariableIncrementalTransformation
import com.sunnychung.lib.multiplatform.bigtext.util.AnnotatedStringBuilder
import com.sunnychung.lib.multiplatform.bigtext.ux.rememberConcurrentLargeAnnotatedBigTextFieldState
import org.junit.jupiter.api.Disabled
import kotlin.test.Test

class DemoTransformationBenchmark {

    @Disabled
    @Test
    fun transform1() {
        val t = BigTextImpl(chunkSize = 2 * 1024 * 1024).apply {
            val initial = TRANSFORMATION_PRELOAD_CONTENT["10 MB"]!!
            append(initial)
        }
        val tt = BigTextTransformerImpl(t).apply {
            setLayouter(MonospaceTextLayouter(FixedWidthCharMeasurer(16f)))
            setSoftWrapEnabled(false)
        }
        val transformation = VariableIncrementalTransformation()
        measureAndPrint("transform") {
            transformation.initialize(t, tt)
        }
    }

    @Disabled
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun transform2() {
        runComposeUiTest {
            setContent {
                val t = BigTextImpl(chunkSize = 2 * 1024 * 1024).apply {
                    val initial = TRANSFORMATION_PRELOAD_CONTENT["10 MB"]!!
                    append(initial)
                }
                val textMeasurer = rememberTextMeasurer(0)
                val tt = BigTextTransformerImpl(
                    t,
                    charSequenceBuilderFactory = { AnnotatedStringBuilder(it) },
                    charSequenceFactory = { (it as AnnotatedStringBuilder).toAnnotatedString() },
                ).apply {
                    setLayouter(MonospaceTextLayouter(ComposeUnicodeCharMeasurer(textMeasurer, TextStyle(fontFamily = FontFamily.Monospace))))
                    setSoftWrapEnabled(false)
                    setContentWidth(1536f)
                }
                val transformation = VariableIncrementalTransformation()
                measureAndPrint("transform") {
                    transformation.initialize(t, tt)
                }
                println("> tree size = ${tt.tree.size()}")
            }
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun transform3() {
        runComposeUiTest {
            setContent {
                println("cp 1")
//                val ts by rememberConcurrentLargeAnnotatedBigTextFieldState(TRANSFORMATION_PRELOAD_CONTENT["10 MB"]!!)
//                val t = ts.text
                val t = BigTextImpl(
                    chunkSize = 2 * 1024 * 1024,
                    textBufferFactory = { AnnotatedStringTextBuffer(it) },
                    charSequenceBuilderFactory = { AnnotatedStringBuilder(it) },
                    charSequenceFactory = { (it as AnnotatedStringBuilder).toAnnotatedString() }
                ).apply {
                    println("cp 1.1")
                    val initial = TRANSFORMATION_PRELOAD_CONTENT["10 MB"]!!
                    println("cp 1.1.2")
                    append(AnnotatedString(initial))
//                    isUndoEnabled = true
                }
                val textMeasurer = rememberTextMeasurer(0)
                println("cp 2")
                val tt = BigTextTransformerImpl(
                    t,
                    charSequenceBuilderFactory = { AnnotatedStringBuilder(it) },
                    charSequenceFactory = { (it as AnnotatedStringBuilder).toAnnotatedString() },
                ).apply {
                    setLayouter(MonospaceTextLayouter(ComposeUnicodeCharMeasurer(textMeasurer, TextStyle(fontFamily = FontFamily.Monospace))))
                    setSoftWrapEnabled(false)
                    setContentWidth(1536f)
                }
//                val ctt = ConcurrentBigTextTransformed(tt)
                val transformation = VariableIncrementalTransformation()
                println("cp 3")
                measureAndPrint("transform") {
                    transformation.initialize(t, tt)
                }
                println("> tree size = ${tt.tree.size()}")
            }
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun transformAndLayout() {
        runComposeUiTest {
            setContent {
//                val ts by rememberConcurrentLargeAnnotatedBigTextFieldState(TRANSFORMATION_PRELOAD_CONTENT["10 MB"]!!)
//                val t = ts.text
                val t = BigTextImpl(
                    chunkSize = 2 * 1024 * 1024,
                    textBufferFactory = { AnnotatedStringTextBuffer(it) },
                    charSequenceBuilderFactory = { AnnotatedStringBuilder(it) },
                    charSequenceFactory = { (it as AnnotatedStringBuilder).toAnnotatedString() }
                ).apply {
                    val initial = TRANSFORMATION_PRELOAD_CONTENT["10 MB"]!!
                    append(AnnotatedString(initial))
//                    isUndoEnabled = true
                }
                val textMeasurer = rememberTextMeasurer(0)
                val tt = BigTextTransformerImpl(
                    t,
                    charSequenceBuilderFactory = { AnnotatedStringBuilder(it) },
                    charSequenceFactory = { (it as AnnotatedStringBuilder).toAnnotatedString() },
                ).apply {
                    setLayouter(MonospaceTextLayouter(ComposeUnicodeCharMeasurer(textMeasurer, TextStyle(fontFamily = FontFamily.Monospace))))
                    setSoftWrapEnabled(true)
                    setContentWidth(1536f)
                }
//                val ctt = ConcurrentBigTextTransformed(tt)
                val transformation = VariableIncrementalTransformation()
                measureAndPrint("transform and layout") {
                    transformation.initialize(t, tt)
                }
                println("> tree size = ${tt.tree.size()}")
            }
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun transformThenLayout() {
        runComposeUiTest {
            setContent {
//                val ts by rememberConcurrentLargeAnnotatedBigTextFieldState(TRANSFORMATION_PRELOAD_CONTENT["10 MB"]!!)
//                val t = ts.text
                val t = BigTextImpl(
                    chunkSize = 2 * 1024 * 1024,
                    textBufferFactory = { AnnotatedStringTextBuffer(it) },
                    charSequenceBuilderFactory = { AnnotatedStringBuilder(it) },
                    charSequenceFactory = { (it as AnnotatedStringBuilder).toAnnotatedString() }
                ).apply {
                    val initial = TRANSFORMATION_PRELOAD_CONTENT["10 MB"]!!
                    append(AnnotatedString(initial))
//                    isUndoEnabled = true
                }
                val textMeasurer = rememberTextMeasurer(0)
                val tt = BigTextTransformerImpl(
                    t,
                    charSequenceBuilderFactory = { AnnotatedStringBuilder(it) },
                    charSequenceFactory = { (it as AnnotatedStringBuilder).toAnnotatedString() },
                ).apply {
                    setLayouter(MonospaceTextLayouter(ComposeUnicodeCharMeasurer(textMeasurer, TextStyle(fontFamily = FontFamily.Monospace))))
                    setSoftWrapEnabled(false)
                    setContentWidth(1536f)
                }
//                val ctt = ConcurrentBigTextTransformed(tt)
                val transformation = VariableIncrementalTransformation()
                measureAndPrint("transform") {
                    transformation.initialize(t, tt)
                }
                measureAndPrint("layout") {
                    tt.setSoftWrapEnabled(true)
                }
                println("> tree size = ${tt.tree.size()}")
            }
        }
    }
}
