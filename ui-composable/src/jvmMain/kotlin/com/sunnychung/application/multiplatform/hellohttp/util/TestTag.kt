package com.sunnychung.application.multiplatform.hellohttp.util

fun buildTestTag(vararg parts: Any?): String? {
    if (parts.any { it == null }) {
        return null
    }
    return parts.joinToString("/")
}
