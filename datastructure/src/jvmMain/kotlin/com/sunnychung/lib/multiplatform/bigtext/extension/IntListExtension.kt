package com.sunnychung.lib.multiplatform.bigtext.extension

import com.sunnychung.lib.multiplatform.bigtext.util.IntList

fun IntList.binarySearchForMinIndexOfValueAtLeast(searchValue: Int)
    = binarySearchForInsertionPoint(indices) { if (get(it) >= searchValue) 1 else -1 }

fun IntList.binarySearchForMaxIndexOfValueAtMost(searchValue: Int)
    = binarySearchForMaxIndexOfValueAtMost(indices, searchValue) { get(it) }
