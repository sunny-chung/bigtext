package com.sunnychung.lib.multiplatform.bigtext.ux.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.unit.Constraints

data class DebugMeasurementModifier(val key: String) : ModifierNodeElement<DebugMeasurementModifierNode>() {
    override fun create(): DebugMeasurementModifierNode = DebugMeasurementModifierNode(key)

    override fun update(node: DebugMeasurementModifierNode) {
        node.key = key
    }
}

class DebugMeasurementModifierNode(var key: String) : Modifier.Node(), LayoutModifierNode {
    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        println("[$key] incoming constraints = $constraints")
        val placeable = measurable.measure(constraints)
        println("[$key] measured = ${placeable.width} * ${placeable.height}, ${placeable.measuredWidth} * ${placeable.measuredHeight}")
        return layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }
}

fun Modifier.debugConstraints(key: String) = this.then(DebugMeasurementModifier(key))
//fun Modifier.debugConstraints(key: String) = this
