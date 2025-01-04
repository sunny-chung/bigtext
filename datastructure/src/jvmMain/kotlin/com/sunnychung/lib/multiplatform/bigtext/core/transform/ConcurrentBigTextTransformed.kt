package com.sunnychung.lib.multiplatform.bigtext.core.transform

import com.sunnychung.lib.multiplatform.bigtext.core.BigText
import com.sunnychung.lib.multiplatform.bigtext.core.BigTextChangeHook
import com.sunnychung.lib.multiplatform.bigtext.core.BigTextNodeValue
import com.sunnychung.lib.multiplatform.bigtext.core.ConcurrentBigText
import com.sunnychung.lib.multiplatform.bigtext.core.LockableBigText
import kotlin.concurrent.read
import kotlin.concurrent.write

class ConcurrentBigTextTransformed<T>(override val delegate: T) : BigTextTransformed, ConcurrentBigText(delegate) where T : BigTextTransformed, T : LockableBigText {

    private val changeHook = object : BigTextChangeHook {
        override fun afterInsertChunk(modifiedText: BigText, position: Int, newValue: BigTextNodeValue) {
            insertOriginal(position, newValue)
        }
        override fun afterDelete(modifiedText: BigText, position: IntRange) {
            deleteOriginal(position)
        }
    }

    init {
        delegate.originalText.layouter?.let { setLayouter(it) }
        delegate.originalText.contentWidth?.let { setContentWidth(it) }
        delegate.unbindChangeHook()
        delegate.originalText.registerBigTextChangeHook(changeHook)
    }

    override val originalText: BigText
        get() = withReadLock { delegate.originalText }

    override val originalLength: Int
        get() = withReadLock { delegate.originalLength }

    override fun findTransformedPositionByOriginalPosition(originalPosition: Int): Int
        = withReadLock { delegate.findTransformedPositionByOriginalPosition(originalPosition) }

    override fun findOriginalPositionByTransformedPosition(transformedPosition: Int): Int
        = withReadLock { delegate.findOriginalPositionByTransformedPosition(transformedPosition) }

    override fun findFirstRowIndexByOriginalLineIndex(originalLineIndex: Int): Int
        = withReadLock { delegate.findFirstRowIndexByOriginalLineIndex(originalLineIndex) }

    override fun findOriginalLineIndexByRowIndex(rowIndex: Int): Int
        = withReadLock { delegate.findOriginalLineIndexByRowIndex(rowIndex) }

    override fun requestReapplyTransformation(originalRange: IntRange)
        = lock.write { delegate.requestReapplyTransformation(originalRange) }

    override fun insertOriginal(
        pos: Int,
        nodeValue: BigTextNodeValue,
        bufferOffsetStart: Int,
        bufferOffsetEndExclusive: Int
    ) = withWriteLock { delegate.insertOriginal(pos, nodeValue, bufferOffsetStart, bufferOffsetEndExclusive) }

    override fun deleteOriginal(originalRange: IntRange, isReMapPositionNeeded: Boolean)
        = withWriteLock { delegate.deleteOriginal(originalRange, isReMapPositionNeeded) }

    override fun replace(range: IntRange, text: CharSequence, offsetMapping: BigTextTransformOffsetMapping)
        = withWriteLock { delegate.replace(range, text, offsetMapping) }

    override fun restoreToOriginal(range: IntRange)
        = withWriteLock { delegate.restoreToOriginal(range) }

    override val maxLineWidth: Long
        get() = withReadLock { delegate.maxLineWidth }

    override var onLayoutCallback: (() -> Unit)?
        get() = delegate.onLayoutCallback // locking getter has no benefit but performance degrade
        set(value) { withWriteLock { delegate.onLayoutCallback = value } }

    override fun findRowPositionStartIndexByRowIndex(index: Int): Int
        = withReadLock { delegate.findRowPositionStartIndexByRowIndex(index) }

    override fun findRowIndexByPosition(position: Int): Int
        = withReadLock { delegate.findRowIndexByPosition(position) }

    override fun findPositionByRowIndex(index: Int): Int
        = withReadLock { delegate.findPositionByRowIndex(index) }

    override fun findWidthByColumnRangeOfSameLine(lineIndex: Int, columns: IntRange): Float
        = withReadLock { delegate.findWidthByColumnRangeOfSameLine(lineIndex, columns) }

    override fun findWidthByPositionRangeOfSameLine(positions: IntRange): Float
        = withReadLock { delegate.findWidthByPositionRangeOfSameLine(positions) }

    override fun findMaxEndPositionOfWidthSumOverPositionRangeAtMost(
        startPosition: Int,
        endPositions: IntRange,
        isEndExclusive: Boolean,
        maxWidthSum: Int
    ): Int
        = withReadLock { delegate.findMaxEndPositionOfWidthSumOverPositionRangeAtMost(
            startPosition = startPosition,
            endPositions = endPositions,
            isEndExclusive = isEndExclusive,
            maxWidthSum = maxWidthSum,
        ) }

    override fun findMinEndPositionOfWidthSumOverPositionRangeAtLeast(
        startPosition: Int,
        endPositions: IntRange,
        isEndExclusive: Boolean,
        minWidthSum: Int
    ): Int
        = withReadLock { delegate.findMinEndPositionOfWidthSumOverPositionRangeAtLeast(
            startPosition = startPosition,
            endPositions = endPositions,
            isEndExclusive = isEndExclusive,
            minWidthSum = minWidthSum,
        ) }

    override fun unbindChangeHook() = delegate.originalText.unregisterBigTextChangeHook(changeHook)
}
