package com.sunnychung.lib.multiplatform.bigtext.core.layout

interface BigTextLayoutable {

    val hasLayouted: Boolean

    val length: Int

    val numOfLines: Int

    val numOfOriginalLines: Int

    val numOfRows: Int

    val lastRowIndex: Int

    val maxLineWidth: Long

    var onLayoutCallback: (() -> Unit)?

    fun setLayouter(layouter: TextLayouter)

    fun setContentWidth(contentWidth: Float)

    fun findRowPositionStartIndexByRowIndex(index: Int): Int

    fun findLineIndexByRowIndex(rowIndex: Int): Int

    fun findRowString(rowIndex: Int): CharSequence

    fun findRowIndexByPosition(position: Int): Int

    fun findPositionByRowIndex(index: Int): Int

    fun findWidthByColumnRangeOfSameLine(lineIndex: Int, columns: IntRange): Float
}
