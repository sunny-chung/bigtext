package com.sunnychung.lib.multiplatform.bigtext.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DebugView() {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(8.dp)) {
        TextGrid("a")
        TextGrid("q")
        TextGrid("A")
        TextGrid("Q")
        TextGrid("⊽")
        TextGrid("⾬")
        TextGrid("g")
        TextGrid("\uD843\uDF30")
    }
}

@Composable
fun TextGrid(text: String) = with(LocalDensity.current) {
    var boundingBox by remember { mutableStateOf<Rect?>(null) }
    var baseline by remember { mutableStateOf<Float?>(null) }
    var lineTop by remember { mutableStateOf<Float?>(null) }
    var lineBottom by remember { mutableStateOf<Float?>(null) }
    var height by remember { mutableStateOf<Float?>(null) }

    var offsetY by remember { mutableStateOf(0f) }

    val fontSize = 16.sp

    Box(Modifier.padding(top = offsetY.toDp())) {
        Text(text, style = LocalTextStyle.current.copy(lineHeightStyle = LineHeightStyle(trim = LineHeightStyle.Trim.Both, alignment = LineHeightStyle.Alignment.Top)), fontSize = fontSize, onTextLayout = {
            boundingBox = it.getBoundingBox(0)
            baseline = it.firstBaseline
            lineBottom = it.getLineBottom(0)
            height = it.size.height.toFloat()
            println("c '$text' bb=$boundingBox bl=$baseline lb=$lineBottom h=$height")

            offsetY = 40.dp.toPx() - (baseline!!)
        })
        boundingBox?.let {
            Line(it.left, it.top, width = it.width, color = Color.Red)
            Line(it.left, it.top, height = it.height, color = Color.Red)
            Line(it.left, it.top + it.height, width = it.width, color = Color.Red)
            Line(it.left + it.width, it.top, height = it.height, color = Color.Red)
        }
        baseline?.let {
            println("bl $it")
            Line(0f, it, boundingBox?.right ?: 100f, color = Color.Blue)
        }
        lineTop?.let {
            println("lt $it")
            Line(0f, it, boundingBox?.right ?: 100f, color = Color.Black)
        }
        lineBottom?.let {
            println("lb $it")
            Line(0f, it, boundingBox?.right ?: 100f, color = Color.Black)
        }
        (lineBottom)?.let {
            Line(0f, it - with(LocalDensity.current) { fontSize.toPx() }, boundingBox?.right ?: 100f, color = Color.Gray)
        }
        height?.let {
            println("h $it")
            Line(0f, it, boundingBox?.right ?: 100f, color = Color.Green)
        }
    }
}

@Composable
fun BoxScope.Line(left: Float, top: Float, width: Float = 0f, height: Float = 0f, color: Color) {
    with(LocalDensity.current) {
        Box(modifier = Modifier
            .padding(start = left.coerceAtLeast(0f).toDp().also { println("l=$it") }, top = top.coerceAtLeast(0f).toDp().also { println("t=$it") })
            .background(color)
            .width(if (height > 0.1) 1.dp else width.toDp())
            .height(if (width > 0.1) 1.dp else height.toDp())
        )
    }
}
