package com.sunnychung.lib.multiplatform.bigtext.core

import co.touchlab.kermit.Severity
import com.sunnychung.lib.multiplatform.bigtext.core.layout.TextLayouter
import com.sunnychung.lib.multiplatform.bigtext.util.GeneralStringBuilder
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

open class ConcurrentBigText(open val delegate: LockableBigText) : BigText {

    val lock = ReentrantReadWriteLock()
    val preLock = ReentrantReadWriteLock()

    override val length: Int
        get() = withReadLock { delegate.length }
    override val lastIndex: Int
        get() = withReadLock { delegate.lastIndex }
    override val isEmpty: Boolean
        get() = withReadLock { delegate.isEmpty }
    override val isNotEmpty: Boolean
        get() = withReadLock { delegate.isNotEmpty }
    override val hasLayouted: Boolean
        get() = withReadLock { delegate.hasLayouted }
    override val layouter: TextLayouter?
        get() = withReadLock { delegate.layouter }
    override val numOfLines: Int
        get() = withReadLock { delegate.numOfLines }
    override val numOfRows: Int
        get() = withReadLock { delegate.numOfRows }
    override val lastRowIndex: Int
        get() = withReadLock { delegate.lastRowIndex }
    override val numOfOriginalLines: Int
        get() = withReadLock { delegate.numOfOriginalLines }
    override val chunkSize: Int
        get() = withReadLock { delegate.chunkSize }
    override val undoHistoryCapacity: Int
        get() = withReadLock { delegate.undoHistoryCapacity }
    override val textBufferFactory: (capacity: Int) -> TextBuffer
        get() = withReadLock { delegate.textBufferFactory }
    override val charSequenceBuilderFactory: (capacity: Int) -> GeneralStringBuilder
        get() = withReadLock { delegate.charSequenceBuilderFactory }
    override val charSequenceFactory: (Appendable) -> CharSequence
        get() = withReadLock { delegate.charSequenceFactory }
    override val tree: LengthTree<BigTextNodeValue>
        get() = withReadLock { delegate.tree }
    override val contentWidth: Float?
        get() = withReadLock { delegate.contentWidth }
    override var decorator: BigTextDecorator?
        get() = withReadLock { delegate.decorator }
        set(value) { withWriteLock { delegate.decorator = value } }
    override var undoMetadataSupplier: (() -> Any?)?
        get() = withReadLock { delegate.undoMetadataSupplier }
        set(value) { withWriteLock { delegate.undoMetadataSupplier = value } }
    override var changeHook: BigTextChangeHook?
        get() = withReadLock { delegate.changeHook }
        set(value) { withWriteLock { delegate.changeHook = value } }

    override val isThreadSafe: Boolean
        get() = true

    override fun buildString(): String = withReadLock { delegate.buildString() }

    override fun buildCharSequence(): CharSequence = withReadLock { delegate.buildCharSequence() }

    override fun substring(start: Int, endExclusive: Int): CharSequence = withReadLock { delegate.substring(start, endExclusive) }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = withReadLock { delegate.subSequence(startIndex, endIndex) }
    override fun chunkAt(start: Int): String = withReadLock { delegate.chunkAt(start) }

    override fun findLineString(lineIndex: Int): CharSequence = withReadLock { delegate.findLineString(lineIndex) }

    override fun findRowString(rowIndex: Int): CharSequence = withReadLock { delegate.findRowString(rowIndex) }

    override fun append(text: CharSequence): Int =
        withPreWriteLock { delegate.append(text) { withWriteLock0(it) } }

    override fun insertAt(pos: Int, text: CharSequence): Int =
        withPreWriteLock { delegate.insertAt(pos, text) { withWriteLock0(it) } }

    override fun delete(start: Int, endExclusive: Int): Int =
        withPreWriteLock { delegate.delete(start, endExclusive) { withWriteLock0(it) } }

    override fun replace(start: Int, endExclusive: Int, text: CharSequence) = withPreWriteLock {
        delegate.replace(start, endExclusive, text) { withWriteLock0(it) }
    }

    override fun replace(range: IntRange, text: CharSequence) = withPreWriteLock {
        delegate.replace(range.start, range.endInclusive + 1, text) { withWriteLock0(it) }
    }

    override fun recordCurrentChangeSequenceIntoUndoHistory() =
        withPreWriteLock {
            withWriteLock { delegate.recordCurrentChangeSequenceIntoUndoHistory() }
        }

    override fun undo(callback: BigTextChangeCallback?): Pair<Boolean, Any?> = withPreWriteLock { delegate.undo(callback) { withWriteLock0(it) } }

    override fun redo(callback: BigTextChangeCallback?): Pair<Boolean, Any?> = withPreWriteLock { delegate.redo(callback) { withWriteLock0(it) } }

    override fun isUndoable(): Boolean = withPreReadLock { withReadLock { delegate.isUndoable() } }

    override fun isRedoable(): Boolean = withPreReadLock { withReadLock { delegate.isRedoable() } }

    override fun findLineAndColumnFromRenderPosition(renderPosition: Int): Pair<Int, Int> = withReadLock { delegate.findLineAndColumnFromRenderPosition(renderPosition) }

    override fun findRenderCharIndexByLineAndColumn(lineIndex: Int, columnIndex: Int): Int = withReadLock { delegate.findRenderCharIndexByLineAndColumn(lineIndex, columnIndex) }

    override fun findPositionStartOfLine(lineIndex: Int): Int = withReadLock { delegate.findPositionStartOfLine(lineIndex) }

    override fun findLineIndexByRowIndex(rowIndex: Int): Int = withReadLock { delegate.findLineIndexByRowIndex(rowIndex) }

    override fun findFirstRowIndexOfLine(lineIndex: Int): Int = withReadLock { delegate.findFirstRowIndexOfLine(lineIndex) }

    override fun setLayouter(layouter: TextLayouter) = withWriteLock { delegate.setLayouter(layouter) }

    override fun setContentWidth(contentWidth: Float) = withWriteLock { delegate.setContentWidth(contentWidth) }

    override fun setSoftWrapEnabled(isSoftWrapEnabled: Boolean) = withWriteLock { delegate.setSoftWrapEnabled(isSoftWrapEnabled) }

    override fun layout() = withWriteLock { delegate.layout() }

    override fun disableComputations() = withWriteLock { delegate.disableComputations() }

    override fun enableAndDoComputations() = withWriteLock { delegate.enableAndDoComputations() }

    override fun registerCallback(callback: BigTextChangeCallback) = withWriteLock { delegate.registerCallback(callback) }

    override fun unregisterCallback(callback: BigTextChangeCallback) = withWriteLock { delegate.unregisterCallback(callback) }

    // the first call to `hashCode()` would write to cache
//    override fun hashCode(): Int = lock.write { delegate.hashCode() }
    // currently, BigTextImpl has no custom implementation over built-in's one, so no lock is needed.
    override fun hashCode(): Int = delegate.hashCode()

//    override fun equals(other: Any?): Boolean = withReadLock { delegate.equals(other) }
    // currently, BigTextImpl has no custom implementation over built-in's one, so no lock is needed.
    override fun equals(other: Any?): Boolean {
        if (other !is ConcurrentBigText) return delegate.equals(other)
        return delegate.equals(other.delegate)
    }

    override fun inspect(label: String): String = withReadLock { delegate.inspect(label) }

    override fun printDebug(label: String) = withReadLock { delegate.printDebug(label) }

    inline fun <R> withWriteLock(operation: (BigText) -> R): R {
        if (log.config.minSeverity <= Severity.Info && lock.isWriteLocked) {
            log.i(Exception("Waiting the write lock to be released in order to acquire a write lock")) { "Waiting the write lock to be released in order to acquire a write lock" }
        }
        return lock.write { operation(delegate) }
    }

    inline fun <R> withReadLock(operation: (BigText) -> R): R {
        if (log.config.minSeverity <= Severity.Info && lock.isWriteLocked) {
            log.i(Exception("Waiting the write lock to be released in order to acquire a read lock")) { "Waiting the write lock to be released in order to acquire a read lock" }
        }
        return lock.read { operation(delegate) }
    }

    private inline fun <R> withWriteLock0(operation: () -> R): R {
        if (log.config.minSeverity <= Severity.Info && lock.isWriteLocked) {
            log.i(Exception("Waiting the write lock to be released in order to acquire a write lock")) { "Waiting the write lock to be released in order to acquire a write lock" }
        }
        return lock.write { operation() }
    }

    private inline fun <R> withReadLock0(operation: () -> R): R {
        if (log.config.minSeverity <= Severity.Info && lock.isWriteLocked) {
            log.i(Exception("Waiting the write lock to be released in order to acquire a read lock")) { "Waiting the write lock to be released in order to acquire a read lock" }
        }
        return lock.read { operation() }
    }

    inline fun <R> withPreWriteLock(operation: () -> R): R {
        if (log.config.minSeverity <= Severity.Info && preLock.isWriteLocked) {
            log.i(Exception("Waiting the pre write lock to be released in order to acquire a write lock")) { "Waiting the pre write lock to be released in order to acquire a write lock" }
        }
        return preLock.write { operation() }
    }

    inline fun <R> withPreReadLock(operation: () -> R): R {
        if (log.config.minSeverity <= Severity.Info && preLock.isWriteLocked) {
            log.i(Exception("Waiting the pre write lock to be released in order to acquire a read lock")) { "Waiting the pre write lock to be released in order to acquire a read lock" }
        }
        return preLock.read { operation() }
    }

    inline fun tryReadLock(operation: (BigText) -> Unit) {
        val isLocked = lock.readLock().tryLock()
        if (isLocked) {
            try {
                operation(delegate)
            } finally {
                lock.readLock().unlock()
            }
        }
    }

}
