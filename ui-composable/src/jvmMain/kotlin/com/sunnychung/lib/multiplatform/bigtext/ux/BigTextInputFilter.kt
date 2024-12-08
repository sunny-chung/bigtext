package com.sunnychung.lib.multiplatform.bigtext.ux

fun interface BigTextInputFilter {

    fun filter(input: CharSequence): CharSequence
}
