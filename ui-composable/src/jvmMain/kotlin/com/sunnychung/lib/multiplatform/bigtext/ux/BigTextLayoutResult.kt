package com.sunnychung.lib.multiplatform.bigtext.ux

import com.sunnychung.lib.multiplatform.bigtext.annotation.TemporaryApi
import com.sunnychung.lib.multiplatform.bigtext.core.layout.BigTextLayoutable
import com.sunnychung.lib.multiplatform.bigtext.core.layout.CharMeasurer
import com.sunnychung.lib.multiplatform.bigtext.extension.binarySearchForMaxIndexOfValueAtMost
import com.sunnychung.lib.multiplatform.bigtext.util.weakRefOf

@OptIn(TemporaryApi::class)
@Deprecated("Slow")
class BigTextLayoutResult(
    /** Number of transformed row spans of non-transformed lines */
    @Deprecated("Slow") @property:TemporaryApi val lineRowSpans: List<Int>, // O(L)
    /** First transformed row index of non-transformed lines */
    @Deprecated("Slow") @property:TemporaryApi val lineFirstRowIndices: List<Int>, // O(L)
    /** Transformed start char index of transformed rows */
    internal val rowStartCharIndices: List<Int>, // O(R)
    @Deprecated("Slow") val rowHeight: Float,
    @Deprecated("Slow") val totalLines: Int,
    @Deprecated("Slow") val totalRows: Int,
    /** Total number of lines before transformation */ val totalLinesBeforeTransformation: Int,
    private val charMeasurer: CharMeasurer<*>,
) {
    @Deprecated("Slow") fun findLineNumberByRowNumber(rowNumber: Int): Int {
        return lineFirstRowIndices.binarySearchForMaxIndexOfValueAtMost(rowNumber)
    }

    @Deprecated("Slow") fun getLineTop(originalLineNumber: Int): Float = lineFirstRowIndices[originalLineNumber] * rowHeight

    @Deprecated("Slow") fun findCharWidth(char: String) = charMeasurer.findCharWidth(char)
}

class BigTextSimpleLayoutResult(
    text: BigTextLayoutable,
    val rowHeight: Float
) {
    private val textRef = weakRefOf(text)
    val text: BigTextLayoutable?
        get() = textRef.get()

    fun getTopOfRow(rowIndex: Int): Float = rowIndex * rowHeight
    fun getBottomOfRow(rowIndex: Int): Float = (rowIndex + 1) * rowHeight

    val top: Float
        get() = 0f

    val bottom: Float
        get() = getBottomOfRow(text?.lastRowIndex ?: 0)
}
