package com.sunnychung.lib.multiplatform.bigtext.ux.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints

fun Modifier.forceHeightAtLeast(minHeight: Int): Modifier = this.layout { measurable, constraints ->
    val minWidth = constraints.minWidth
    val maxWidth = constraints.maxWidth
    val minCHeight = constraints.minHeight.coerceAtLeast(minHeight)
    val maxCHeight = constraints.maxHeight.coerceAtLeast(minHeight).coerceAtLeast(minCHeight)
    val placeable = measurable.measure(Constraints(minWidth, maxWidth, minCHeight, maxCHeight))
//    println("M minHeight=$minCHeight maxHeight=$maxCHeight measured=${placeable.width}*${placeable.height}, ${placeable.measuredWidth}*${placeable.measuredHeight}")
    layout(placeable.width, placeable.height.coerceAtLeast(minCHeight)) {
        placeable.place(0, 0)
    }
}
