package com.sunnychung.application.multiplatform.hellohttp.util

import androidx.compose.ui.text.AnnotatedString

fun CharSequence.string(): String = when (this) {
    is String -> this
    is AnnotatedString -> text
    else -> toString()
}

fun CharSequence.annotatedString(): AnnotatedString = when (this) {
    is String -> AnnotatedString(this)
    is AnnotatedString -> this
    else -> AnnotatedString(toString())
}
