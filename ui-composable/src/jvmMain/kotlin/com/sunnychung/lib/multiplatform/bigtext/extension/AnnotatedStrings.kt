package com.sunnychung.lib.multiplatform.bigtext.extension

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString

fun AnnotatedString.replaceAll(regex: Regex, replacement: String, startAt: Int = 0): AnnotatedString {
    val matches = regex.findAll(text, startAt)
    if (matches.count() == 0) {
        return this
    }
    return buildAnnotatedString {
        var remainCharIndex = 0
        matches.forEach {
            if (it.range.start - remainCharIndex > 0) {
                append(subSequence(remainCharIndex ..< it.range.start))
            }
            append(replacement)
            remainCharIndex = it.range.endInclusive + 1
        }
        if (remainCharIndex <= lastIndex) {
            append(subSequence(remainCharIndex .. lastIndex))
        }
    }
}
