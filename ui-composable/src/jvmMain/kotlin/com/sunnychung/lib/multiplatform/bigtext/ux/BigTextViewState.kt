package com.sunnychung.lib.multiplatform.bigtext.ux

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TransformedText
import com.sunnychung.lib.multiplatform.bigtext.core.transform.BigTextTransformed
import com.sunnychung.lib.multiplatform.bigtext.util.WeakRefKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

val EMPTY_SELECTION_RANGE = 0 .. -1

class BigTextViewState {
    /**
     * A unique value that changes when the BigText string value is changed.
     *
     * This field is generated randomly and is NOT a sequence number.
     */
    var version: Long by mutableStateOf(0)
        internal set

    @Deprecated("Use calculateVisibleRowRange")
    var firstVisibleRow: Int by mutableStateOf(0)
        internal set

    @Deprecated("Use calculateVisibleRowRange")
    var lastVisibleRow: Int by mutableStateOf(0)
        internal set

    internal var lastCursorXPositionForVerticalMovement by mutableStateOf<Float>(0f)

    internal var isScrollToCursorNeeded by mutableStateOf(false)

    internal var transformedSelection: IntRange by mutableStateOf(0..-1)

    /**
     * `transformedSelectionStart` can be different from `transformedSelection.start`.
     * If a text is selected from position 5 to 1, transformedSelection = (1 .. 5) while transformedSelectionStart = 5.
     */
    var transformedSelectionStart: Int by mutableStateOf(0)
        internal set

    var selection: IntRange by mutableStateOf(0..-1)
        internal set

    fun hasSelection(): Boolean = !selection.isEmpty() && transformedSelection.start >= 0 && !transformedSelection.isEmpty()

    internal fun updateSelectionByTransformedSelection(transformedText: TransformedText) {
        selection = transformedText.offsetMapping.transformedToOriginal(transformedSelection.first) ..
                transformedText.offsetMapping.transformedToOriginal(transformedSelection.last)
    }

    internal fun updateTransformedSelectionBySelection(transformedText: TransformedText) {
        transformedSelection = transformedText.offsetMapping.originalToTransformed(selection.first) ..
                transformedText.offsetMapping.originalToTransformed(selection.last)
    }

    internal fun updateSelectionByTransformedSelection(transformedText: BigTextTransformed) {
        selection = if (transformedSelection.isEmpty()) {
            EMPTY_SELECTION_RANGE
        } else {
            transformedText.findOriginalPositionByTransformedPosition(transformedSelection.first) ..
                transformedText.findOriginalPositionByTransformedPosition(transformedSelection.last)
        }
    }

    internal fun updateTransformedSelectionBySelection(transformedText: BigTextTransformed) {
        transformedSelection = if (!selection.isEmpty()) {
            transformedText.findTransformedPositionByOriginalPosition(selection.first) ..
                    transformedText.findTransformedPositionByOriginalPosition(selection.last)
        } else {
            IntRange.EMPTY
        }
    }

    internal var transformedCursorIndex by mutableStateOf(0)
    var cursorIndex by mutableStateOf(0)
        internal set

    internal fun updateCursorIndexByTransformed(transformedText: TransformedText) {
        cursorIndex = transformedText.offsetMapping.transformedToOriginal(transformedCursorIndex)
    }

    internal fun updateTransformedCursorIndexByOriginal(transformedText: TransformedText) {
        transformedCursorIndex = transformedText.offsetMapping.originalToTransformed(cursorIndex)
    }

    internal fun updateCursorIndexByTransformed(transformedText: BigTextTransformed) {
        cursorIndex = transformedText.findOriginalPositionByTransformedPosition(transformedCursorIndex).also {
            com.sunnychung.lib.multiplatform.bigtext.util.log.d { "cursorIndex = $it (from T $transformedCursorIndex)" }
        }
    }

    internal fun updateTransformedCursorIndexByOriginal(transformedText: BigTextTransformed) {
        transformedCursorIndex = transformedText.findTransformedPositionByOriginalPosition(cursorIndex).also {
            com.sunnychung.lib.multiplatform.bigtext.util.log.d { "updateTransformedCursorIndexByOriginal = $it (from $cursorIndex)" }
        }
        cursorIndex = transformedText.findOriginalPositionByTransformedPosition(transformedCursorIndex)
    }

    fun setCursorIndex(newCursorIndex: Int) {
        cursorIndex = newCursorIndex
        transformedText?.get()?.let { transformedText ->
            updateTransformedCursorIndexByOriginal(transformedText)
        }
        recordCursorXPosition()
        isScrollToCursorNeeded = true
    }

    fun setSelection(range: IntRange) {
        val transformedText = transformedText?.get() ?: run {
            // if transformedText is not available, set the selection without checking the upper bound
            require(range.start >= 0) { "Range start ${range.start} is invalid" }
            require(range.endInclusive + 1 >= 0) { "Range end ${range.endInclusive} is invalid" }
            selection = range
            return
        }
        val text = transformedText.originalText
        require(range.start in 0 .. text.length) { "Range start ${range.start} is out of range. Text length: ${text.length}" }
        require(range.endInclusive + 1 in 0 .. text.length) { "Range end ${range.endInclusive} is out of range. Text length: ${text.length}" }

        selection = range
        updateTransformedSelectionBySelection(transformedText)
    }

    internal fun roundTransformedCursorIndex(direction: CursorAdjustDirection, transformedText: BigTextTransformed, compareWithPosition: Int, isOnlyWithinBlock: Boolean) {
        transformedCursorIndex = roundedTransformedCursorIndex(transformedCursorIndex, direction, transformedText, compareWithPosition, isOnlyWithinBlock).also {
            com.sunnychung.lib.multiplatform.bigtext.util.log.d { "roundedTransformedCursorIndex($transformedCursorIndex, $direction, ..., $compareWithPosition) = $it" }
        }
    }

    internal fun roundedTransformedCursorIndex(transformedCursorIndex: Int, direction: CursorAdjustDirection, transformedText: BigTextTransformed, compareWithPosition: Int, isOnlyWithinBlock: Boolean): Int {
        val possibleRange = 0 .. transformedText.length
        val previousMappedPosition = transformedText.findOriginalPositionByTransformedPosition(compareWithPosition)
        when (direction) {
            CursorAdjustDirection.Forward, CursorAdjustDirection.Backward -> {
                val step = if (direction == CursorAdjustDirection.Forward) 1 else -1
                var delta = 0
                while (transformedCursorIndex + delta in possibleRange) {
                    if (transformedText.findOriginalPositionByTransformedPosition(transformedCursorIndex + delta) != previousMappedPosition) {
                        val newPos = transformedCursorIndex + delta + if (isOnlyWithinBlock) {
                            // for backward, we find the last index that is same as `previousMappedPosition`
                            - step
                        } else {
                            // for forward, we find the first index that is different from `previousMappedPosition`
                            0
                        }
                        if (newPos == transformedText.length) {
                            return newPos
                        }
                        val char = transformedText.subSequence(newPos, newPos + 1)
                        if (
                            (direction == CursorAdjustDirection.Forward && !char[0].isLowSurrogate())
                            || (direction == CursorAdjustDirection.Backward && !char[0].isLowSurrogate())
                        ) {
                            return newPos
                        }
                    }
                    delta += step
                }
                // (transformedCursorIndex + delta) is out of range
                return transformedCursorIndex + delta - step
            }
            CursorAdjustDirection.Bidirectional -> {
                if (transformedCursorIndex >= transformedText.length) {
                    return transformedText.length
                }

                // First, try without offset
                if (transformedCursorIndex != compareWithPosition && transformedCursorIndex in possibleRange && transformedText.findOriginalPositionByTransformedPosition(transformedCursorIndex) != previousMappedPosition) {
                    val newPos = transformedCursorIndex
                    val char = transformedText.subSequence(newPos, newPos + 1)
                    if (!char[0].isLowSurrogate()) {
                        return newPos
                    }
                }

                var delta = 0
                while ((transformedCursorIndex + delta in possibleRange || transformedCursorIndex - delta in possibleRange)) {
                    // Try in both forward and backward directions

                    // Forward
                    if (transformedCursorIndex + delta + 1 in possibleRange && transformedText.findOriginalPositionByTransformedPosition(transformedCursorIndex + delta + 1) != previousMappedPosition) {
                        val newPos = transformedCursorIndex + delta + if (transformedCursorIndex + delta - 1 in possibleRange && transformedText.findOriginalPositionByTransformedPosition(transformedCursorIndex + delta - 1) == previousMappedPosition) {
                            // position (transformedCursorIndex + delta) is a block,
                            // while position (transformedCursorIndex + delta + 1) is not a block.
                            // so return (transformedCursorIndex + delta + 1)
                            1
                        } else {
                            0
                        }
                        if (newPos == transformedText.length) {
                            return newPos
                        }
                        val char = transformedText.subSequence(newPos, newPos + 1)
                        if (!char[0].isLowSurrogate()) {
                            return newPos
                        }
                    }
                    // Backward
                    if (transformedCursorIndex - delta - 1 in possibleRange && transformedText.findOriginalPositionByTransformedPosition(transformedCursorIndex - delta - 1) != previousMappedPosition) {
                        // for backward, we find the last index that is same as `previousMappedPosition`
                        val newPos = transformedCursorIndex - delta //+ 1
                        val char = transformedText.subSequence(newPos, newPos + 1)
                        if (!char[0].isLowSurrogate()) {
                            return newPos
                        }
                    }
                    ++delta
                }
                return transformedCursorIndex + delta - 1
            }
        }
    }

    fun recordCursorXPosition() {
        val transformedText = transformedText?.get() ?: return
        val row = transformedText.findRowIndexByPosition(transformedCursorIndex)
        val rowStart = transformedText.findRowPositionStartIndexByRowIndex(row)
        log.d { "recordCursorXPosition Tcur=${transformedCursorIndex} row=$row rowStart=$rowStart" }
        val cursorXPosInRow = transformedText.findWidthByPositionRangeOfSameLine(rowStart ..< transformedCursorIndex)
        lastCursorXPositionForVerticalMovement = cursorXPosInRow
    }

    private val charRangesToReapplyTransforms = mutableSetOf<IntRange>()

    fun requestReapplyTransformation(originalRange: IntRange) {
        com.sunnychung.lib.multiplatform.bigtext.util.log.d { "requestReapplyTransformation $originalRange" }
        charRangesToReapplyTransforms += originalRange
    }

    fun pollReapplyTransformCharRanges(): List<IntRange> {
        val result = charRangesToReapplyTransforms.toList()
        charRangesToReapplyTransforms.clear()
        return result
    }

    var transformedText: WeakRefKey<BigTextTransformed>? = null
        internal set(value) {
            field = value

            val transformedText = value?.get() ?: return
            updateTransformedCursorIndexByOriginal(transformedText)
            updateTransformedSelectionBySelection(transformedText)
        }

    private val isLayoutDisabledMutableStateFlow = MutableStateFlow(false)

    val isLayoutDisabledFlow: Flow<Boolean> = isLayoutDisabledMutableStateFlow

    var isLayoutDisabled: Boolean
        get() = isLayoutDisabledMutableStateFlow.value
        set(value) {
            isLayoutDisabledMutableStateFlow.value = value
        }

    var layoutResult: BigTextSimpleLayoutResult? = null
        internal set

    var visibleSize: Size = Size(0, 0)
        internal set

    var numOfComputationsInProgress by mutableStateOf(0)
        internal set

    val isComponentReady: Boolean
        get() = numOfComputationsInProgress <= 0

    /**
     * The returned value of this function can be more recent or accurate than the `firstVisibleRow` and `lastVisibleRow` values.
     *
     * Note that if dependencies are not yet available, this function returns `0 .. 0`.
     */
    fun calculateVisibleRowRange(verticalScrollValue: Int): IntRange {
        if (!isComponentReady) {
            return 0 .. -1
        }

        val transformedText = transformedText?.get() ?: return 0 .. 0
        val viewportTop = verticalScrollValue.toFloat()
        val height = visibleSize.height
        val lineHeight = layoutResult?.rowHeight ?: return 0 .. 0
        val viewportBottom = viewportTop + height
        val firstRowIndex = maxOf(0, (viewportTop / lineHeight).toInt())
        val lastRowIndex = minOf(transformedText.lastRowIndex, (viewportBottom / lineHeight).toInt() + 1)
        return firstRowIndex .. lastRowIndex
    }

    fun findRelativeXYOfOriginalCharIndex(originalCharIndex: Int, verticalScrollValue: Int, horizontalScrollValue: Int): Pair<Float, Float>? {
        if (!isComponentReady) return null
        val transformedText = transformedText?.get() ?: return null
        val layoutResult = layoutResult ?: return null
        val transformedCharIndex = transformedText.findTransformedPositionByOriginalPosition(originalCharIndex)
        val row = transformedText.findRowIndexByPosition(transformedCharIndex)
        val y = row * layoutResult.rowHeight - verticalScrollValue

        val rowStart = transformedText.findRowPositionStartIndexByRowIndex(row)
        val absX = transformedText.findWidthByPositionRangeOfSameLine(rowStart ..< transformedCharIndex)
        val x = absX - horizontalScrollValue

        return x to y
    }

    fun toImmutable(): ImmutableBigTextViewState = ImmutableBigTextViewState(this)
}

data class Size(val width: Int, val height: Int)

data class ImmutableBigTextViewState(
    val version: Long = 0,
    val firstVisibleRow: Int = 0,
    val lastVisibleRow: Int = 0,
    internal val transformedSelection: IntRange = 0 .. -1,
    val transformedSelectionStart: Int = 0,
    val selection: IntRange = 0 .. -1,
    internal val transformedCursorIndex: Int = 0,
    val cursorIndex: Int = 0,
    val transformText: WeakRefKey<BigTextTransformed>? = null,
) {
    constructor(s: BigTextViewState) : this(
        version = s.version,
        firstVisibleRow = s.firstVisibleRow,
        lastVisibleRow = s.lastVisibleRow,
        transformedSelection = s.transformedSelection,
        transformedSelectionStart = s.transformedSelectionStart,
        selection = s.selection,
        transformedCursorIndex = s.transformedCursorIndex,
        cursorIndex = s.cursorIndex,
        transformText = s.transformedText,
    )
}
