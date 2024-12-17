package com.sunnychung.lib.multiplatform.bigtext.ux

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.AnnotatedString
import com.sunnychung.lib.multiplatform.bigtext.core.BigText
import com.sunnychung.lib.multiplatform.bigtext.core.ConcurrentBigText

/**
 * Create a BigTextFieldState with a thread-safe large text buffer. Specifically, only the `BigText` class is thread-safe.
 *
 * `BigTextViewState` is NOT thread-safe and can only be manipulated in UI thread.
 *
 * The argument `initialValue` is only used when there is a cache miss using the cache key `cacheKey`.
 */
@Composable
fun rememberConcurrentLargeAnnotatedBigTextFieldState(initialValue: String = "", vararg cacheKeys: Any?, initialize: (BigTextFieldState) -> Unit = {}): MutableState<BigTextFieldState> {
    return rememberConcurrentLargeAnnotatedBigTextFieldState(
        initialValue = AnnotatedString(initialValue),
        cacheKeys = cacheKeys,
        initialize = initialize
    )
}

/**
 * Create a BigTextFieldState with a thread-safe large text buffer. Specifically, only the `BigText` class is thread-safe.
 *
 * `BigTextViewState` is NOT thread-safe and can only be manipulated in UI thread.
 *
 * The argument `initialValue` is only used when there is a cache miss using the cache key `cacheKey`.
 */
@Composable
fun rememberConcurrentLargeAnnotatedBigTextFieldState(initialValue: AnnotatedString, vararg cacheKeys: Any?, initialize: (BigTextFieldState) -> Unit = {}): MutableState<BigTextFieldState> {
    return rememberSaveable(*cacheKeys) {
        log.i { "cache miss concurrent 1" }
        mutableStateOf(
            BigTextFieldState(
                ConcurrentBigText(BigText.createFromLargeAnnotatedString(initialValue)),
                BigTextViewState()
            ).apply(initialize)
        )
    }
}
