package com.sunnychung.lib.multiplatform.bigtext.ux.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints

fun Modifier.forceHeightAtLeast(minHeight: Int): Modifier = this.layout { measurable, constraints ->
    val minWidth = constraints.minWidth
    val maxWidth = constraints.maxWidth
    val minHeight = constraints.minHeight.coerceAtLeast(minHeight)
    val maxHeight = constraints.maxHeight.coerceAtLeast(minHeight)
    val placeable = measurable.measure(Constraints(minWidth, maxWidth, minHeight, maxHeight))
//    println("M minHeight=$minHeight maxHeight=$maxHeight measured=${placeable.width}*${placeable.height}, ${placeable.measuredWidth}*${placeable.measuredHeight}")
    layout(placeable.width, placeable.height) {
        placeable.place(0, 0)
    }
}
