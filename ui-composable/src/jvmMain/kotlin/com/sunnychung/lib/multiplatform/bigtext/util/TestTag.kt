package com.sunnychung.lib.multiplatform.bigtext.util

fun buildTestTag(vararg parts: Any?): String? {
    if (parts.any { it == null }) {
        return null
    }
    return parts.joinToString("/")
}
