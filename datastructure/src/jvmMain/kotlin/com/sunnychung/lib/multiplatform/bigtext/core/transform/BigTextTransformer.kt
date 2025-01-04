package com.sunnychung.lib.multiplatform.bigtext.core.transform

interface BigTextTransformer {

//    fun resetToOriginal()

//    fun applyStyle(style: SpanStyle, range: IntRange)

    fun append(text: CharSequence): Int

    fun insertAt(pos: Int, text: CharSequence): Int

    fun delete(range: IntRange): Int

    fun replace(range: IntRange, text: CharSequence, offsetMapping: BigTextTransformOffsetMapping)

    fun restoreToOriginal(range: IntRange)

//    fun layoutTransaction(transaction: BigTextLayoutTransaction.() -> Unit)

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

    /**
     * Not for general use.
     */
    fun unbindChangeHook()
}
