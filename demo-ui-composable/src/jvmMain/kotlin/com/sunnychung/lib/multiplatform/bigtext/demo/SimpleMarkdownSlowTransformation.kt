package com.sunnychung.lib.multiplatform.bigtext.demo

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import com.sunnychung.lib.multiplatform.bigtext.core.BigText
import com.sunnychung.lib.multiplatform.bigtext.core.BigTextChangeEvent
import com.sunnychung.lib.multiplatform.bigtext.core.transform.BigTextTransformOffsetMapping
import com.sunnychung.lib.multiplatform.bigtext.core.transform.BigTextTransformer
import com.sunnychung.lib.multiplatform.bigtext.core.transform.IncrementalTextTransformation

class SimpleMarkdownSlowTransformation : IncrementalTextTransformation<Unit> {
    private val HEADER_SPAN_STYLE = SpanStyle(fontWeight = FontWeight.Bold)

    override fun initialize(text: BigText, transformer: BigTextTransformer) {
        transform(text, transformer)
    }

    private fun transform(text: BigText, transformer: BigTextTransformer) {
        log.d { "Highlight transform" }
        transformer.restoreToOriginal(0 .. text.length)
        "^\\s*#[^\n]+".toRegex(RegexOption.MULTILINE).findAll(text.buildString()).forEach { match ->
            log.d { "Highlight ${match.range}" }
            transformer.replace(match.range, AnnotatedString(match.value, HEADER_SPAN_STYLE), BigTextTransformOffsetMapping.Incremental)
        }
    }

    override fun afterTextChange(change: BigTextChangeEvent, transformer: BigTextTransformer, context: Unit) {
        transform(change.bigText, transformer)
    }
}
