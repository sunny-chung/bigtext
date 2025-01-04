package com.sunnychung.lib.multiplatform.bigtext.demo

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import co.touchlab.kermit.Severity
import com.sunnychung.lib.multiplatform.bigtext.core.BigText
import com.sunnychung.lib.multiplatform.bigtext.core.BigTextChangeEvent
import com.sunnychung.lib.multiplatform.bigtext.core.transform.BigTextTransformOffsetMapping
import com.sunnychung.lib.multiplatform.bigtext.core.transform.BigTextTransformed
import com.sunnychung.lib.multiplatform.bigtext.core.transform.BigTextTransformer
import com.sunnychung.lib.multiplatform.bigtext.core.transform.IncrementalTextTransformation

/**
 * This Transformation is AS SLOW AS using BasicTextField.
 *
 * To utilize the power of BigText, incremental transformation should be used instead.
 */
class SimpleMarkdownSlowTransformation : IncrementalTextTransformation<Unit> {
    private val HEADER_SPAN_STYLE = SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)
    private val BOLD_SPAN_STYLE = SpanStyle(fontWeight = FontWeight.Bold)
    private val ITALICS_SPAN_STYLE = SpanStyle(fontStyle = FontStyle.Italic)
    private val STRIKETHROUGH_SPAN_STYLE = SpanStyle(textDecoration = TextDecoration.LineThrough)
    private val CODE_SPAN_STYLE = SpanStyle(fontFamily = FontFamily.Monospace)

    private val WORD_REGEX_COMPONENT = "[^\n\\*_`]"

    private val SYNTAX_STYLE_MAP: Map<Regex, SpanStyle> = mapOf(
        "^(#$WORD_REGEX_COMPONENT+)" to HEADER_SPAN_STYLE,
        "\\*($WORD_REGEX_COMPONENT+)\\*" to BOLD_SPAN_STYLE,
        "\\_($WORD_REGEX_COMPONENT+)\\_" to ITALICS_SPAN_STYLE,
        "\\~($WORD_REGEX_COMPONENT+)\\~" to STRIKETHROUGH_SPAN_STYLE,
        "\\`($WORD_REGEX_COMPONENT+)\\`" to CODE_SPAN_STYLE,
    ).mapKeys { it.key.toRegex(RegexOption.MULTILINE) }

    override fun initialize(text: BigText, transformer: BigTextTransformer) {
        transform(text, transformer)
    }

    private fun transform(text: BigText, transformer: BigTextTransformer) {
        log.d { "Highlight transform" }
        transformer.restoreToOriginal(0 .. text.length)
        val fullText = text.buildString()
        SYNTAX_STYLE_MAP.forEach { (regex, spanStyle) ->
            regex.findAll(fullText).forEach { match ->
                val matchText = match.groups[1]!!
                log.d { "Highlight ${match.range} ${matchText.range}" }
                if (match.range.start < matchText.range.start) {
                    transformer.delete(match.range.start ..< matchText.range.start)
                }
                transformer.replace(matchText.range, AnnotatedString(matchText.value, spanStyle), BigTextTransformOffsetMapping.Incremental)
                if (match.range.endInclusive > matchText.range.endInclusive) {
                    transformer.delete(matchText.range.endInclusive + 1 .. match.range.endInclusive)
                }
            }
        }

        if (log.config.minSeverity <= Severity.Debug) {
            (transformer as BigTextTransformed).printDebug("After Transform")
        }
    }

    override fun afterTextChange(change: BigTextChangeEvent, transformer: BigTextTransformer, context: Unit) {
        transform(change.bigText, transformer)
    }
}
