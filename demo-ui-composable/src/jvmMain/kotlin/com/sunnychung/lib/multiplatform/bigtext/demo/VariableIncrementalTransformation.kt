package com.sunnychung.lib.multiplatform.bigtext.demo

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import com.sunnychung.lib.multiplatform.bigtext.core.BigText
import com.sunnychung.lib.multiplatform.bigtext.core.BigTextChangeEvent
import com.sunnychung.lib.multiplatform.bigtext.core.BigTextChangeEventType
import com.sunnychung.lib.multiplatform.bigtext.core.transform.BigTextTransformOffsetMapping
import com.sunnychung.lib.multiplatform.bigtext.core.transform.BigTextTransformer
import com.sunnychung.lib.multiplatform.bigtext.core.transform.IncrementalTextTransformation
import com.sunnychung.lib.multiplatform.bigtext.extension.hasIntersectWith

class VariableIncrementalTransformation : IncrementalTextTransformation<Unit> {
    private val processLengthLimit = 30

    private val variableNameRegex = "[^{}\$\n\r]{1,$processLengthLimit}".toRegex()

    private val variableRegex = "\\$\\{\\{(${variableNameRegex.pattern})\\}\\}".toRegex()

    private val style = SpanStyle(
        color = Color.White,
        background = Color.Blue,
        fontFamily = FontFamily.Monospace,
    )

    override fun initialize(text: BigText, transformer: BigTextTransformer) {
        val targets = variableRegex.findAll(text.buildString())
        targets.forEach {
            val name = it.groups[1]!!.value
            transformer.replace(it.range, createSpan(name), BigTextTransformOffsetMapping.WholeBlock)
        }
    }

    override fun afterTextChange(change: BigTextChangeEvent, transformer: BigTextTransformer, context: Unit) {
        when (change.eventType) {
            BigTextChangeEventType.Insert -> {
                val changeRange = change.changeStartIndex until change.changeEndExclusiveIndex
                val targets = findNearbyPatterns(change)
                targets.filter { it.range hasIntersectWith changeRange }
                    .forEach {
                        val name = it.result.groups[1]!!.value
                        transformer.restoreToOriginal(it.range)
                        transformer.replace(it.range, createSpan(name), BigTextTransformOffsetMapping.WholeBlock)
                    }
            }

            else -> {}
        }
    }

    private fun findNearbyPatterns(change: BigTextChangeEvent): Sequence<RangeWithResult<MatchResult>> {
        val startOffset = maxOf(0, change.changeStartIndex - processLengthLimit)
        val substring = change.bigText.substring(
            startOffset
            until
            minOf(change.bigText.length, change.changeEndExclusiveIndex + processLengthLimit)
        )
        return variableRegex.findAll(substring)
            .map {
                RangeWithResult(
                    range = it.range.start + startOffset .. it.range.endInclusive + startOffset,
                    result = it
                )
            }
    }

    override fun beforeTextChange(change: BigTextChangeEvent, transformer: BigTextTransformer, context: Unit) {
        when (change.eventType) {
            BigTextChangeEventType.Delete -> {
                val changeRange = change.changeStartIndex until change.changeEndExclusiveIndex
                val targets = findNearbyPatterns(change)
                targets.filter { it.range hasIntersectWith changeRange }
                    .forEach {
                        transformer.restoreToOriginal(it.range)
                    }
            }

            else -> {}
        }
    }

    private fun createSpan(name: String): CharSequence {
        return AnnotatedString(name, listOf(AnnotatedString.Range(style, 0, name.length)))
    }
}

class RangeWithResult<T>(val range: IntRange, val result: T)
