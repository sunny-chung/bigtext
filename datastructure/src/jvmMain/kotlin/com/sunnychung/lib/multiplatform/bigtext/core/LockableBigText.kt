package com.sunnychung.lib.multiplatform.bigtext.core

typealias BigTextLocker = (() -> Unit) -> Unit
val PassthroughBigTextLocker: BigTextLocker = { it() }

interface LockableBigText : BigText {
    fun insertAt(pos: Int, text: CharSequence, locker: BigTextLocker?): Int

    fun delete(start: Int, endExclusive: Int, locker: BigTextLocker?): Int

    fun append(text: CharSequence, locker: BigTextLocker?): Int

    fun replace(start: Int, endExclusive: Int, text: CharSequence, locker: BigTextLocker?)

    fun undo(callback: BigTextChangeCallback? = null, locker: BigTextLocker?): Pair<Boolean, Any?>

    fun redo(callback: BigTextChangeCallback? = null, locker: BigTextLocker?): Pair<Boolean, Any?>
}
