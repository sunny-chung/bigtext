package com.sunnychung.lib.multiplatform.bigtext.core

import com.sunnychung.lib.multiplatform.bigtext.core.layout.TextLayouter
import com.sunnychung.lib.multiplatform.bigtext.util.GeneralStringBuilder

/**
 * Manipulates large String.
 */
interface BigText {

    val length: Int

    val lastIndex: Int

    val isEmpty: Boolean

    val isNotEmpty: Boolean

    val hasLayouted: Boolean

    val layouter: TextLayouter?

    val numOfLines: Int

    val numOfRows: Int

    val lastRowIndex: Int

    val numOfOriginalLines: Int

    val chunkSize: Int
    val undoHistoryCapacity: Int
    val textBufferFactory: ((capacity: Int) -> TextBuffer)
    val charSequenceBuilderFactory: ((capacity: Int) -> GeneralStringBuilder)
    val charSequenceFactory: ((Appendable) -> CharSequence)
    val tree: LengthTree<BigTextNodeValue>
    val contentWidth: Float?

    var decorator: BigTextDecorator?

    var undoMetadataSupplier: (() -> Any?)?

    val isThreadSafe: Boolean
        get() = false

    fun buildString(): String

    fun buildCharSequence(): CharSequence

    fun substring(start: Int, endExclusive: Int): CharSequence

    fun substring(range: IntRange): CharSequence = substring(range.start, range.endInclusive + 1)

    fun subSequence(startIndex: Int, endIndex: Int): CharSequence

    fun chunkAt(start: Int): String

    fun findLineString(lineIndex: Int): CharSequence

    fun findRowString(rowIndex: Int): CharSequence

    fun append(text: CharSequence): Int

    fun insertAt(pos: Int, text: CharSequence): Int

    fun delete(start: Int, endExclusive: Int): Int

    fun delete(range: IntRange): Int = delete(range.start, range.endInclusive + 1)

    fun replace(start: Int, endExclusive: Int, text: CharSequence) {
        delete(start, endExclusive)
        insertAt(start, text)
    }

    fun replace(range: IntRange, text: CharSequence) {
        delete(range)
        insertAt(range.start, text)
    }

    fun recordCurrentChangeSequenceIntoUndoHistory()

    fun undo(callback: BigTextChangeCallback? = null): Pair<Boolean, Any?>

    fun redo(callback: BigTextChangeCallback? = null): Pair<Boolean, Any?>

    fun isUndoable(): Boolean

    fun isRedoable(): Boolean

    fun findLineAndColumnFromRenderPosition(renderPosition: Int): Pair<Int, Int>

    fun findRenderCharIndexByLineAndColumn(lineIndex: Int, columnIndex: Int): Int

    fun findPositionStartOfLine(lineIndex: Int): Int

    /**
     * @param rowIndex 0-based
     * @return 0-based
     */
    fun findLineIndexByRowIndex(rowIndex: Int): Int

    /**
     * @param lineIndex 0-based line index
     * @return 0-based row index
     */
    fun findFirstRowIndexOfLine(lineIndex: Int): Int

    fun setLayouter(layouter: TextLayouter)

    fun setContentWidth(contentWidth: Float)

    fun setSoftWrapEnabled(isSoftWrapEnabled: Boolean)

    /**
     * This function should not be called manually in normal routines.
     */
    fun layout()

    /**
     * Disable computations related to layout. Default it is not disabled.
     *
     * This is used to optimize performance when there are a large amount of changes at the same time.
     * It should be enabled back using [enableAndDoComputations()][enableAndDoComputations] once all the changes are applied.
     */
    fun disableComputations()

    /**
     * Enable and execute computations of all the nodes immediately.
     *
     * @see disableComputations
     */
    fun enableAndDoComputations()

    fun registerCallback(callback: BigTextChangeCallback)

    fun unregisterCallback(callback: BigTextChangeCallback)

    /**
     * Not for general use.
     */
    fun registerBigTextChangeHook(hook: BigTextChangeHook)

    /**
     * Not for general use.
     */
    fun unregisterBigTextChangeHook(hook: BigTextChangeHook)

    override fun hashCode(): Int

    override fun equals(other: Any?): Boolean

    fun inspect(label: String = ""): String

    fun printDebug(label: String = "")

    companion object
}
