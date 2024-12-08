package com.sunnychung.lib.multiplatform.bigtext.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.random.Random

val PRELOAD_CONTENT = linkedMapOf(
    "Empty" to "",
    "1 KB" to generateRandomContent(1 * 1024),
    "10 KB" to generateRandomContent(10 * 1024),
    "1 MB" to generateRandomContent(1 * 1024 * 1024),
    "10 MB" to generateRandomContent(10 * 1024 * 1024),
    "100 MB" to generateRandomContent(100 * 1024 * 1024),
)

private enum class DemoView(val displayName: String) {
    Simple("Simple"), LargeCodeEditor("Large Code Editor")
}

@Composable
fun AppView() {
    var chosenDemoView by remember { mutableStateOf(DemoView.Simple) }

    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoView
                .entries
                .forEach {
                    Button(
                        onClick = {
                            chosenDemoView = it
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor =
                            if (it == chosenDemoView) Color.Green
                            else Color.Cyan
                        )
                    ) {
                        Text(text = it.displayName)
                    }
                }
        }
        when (chosenDemoView) {
            DemoView.Simple -> SimpleDemoView()
            DemoView.LargeCodeEditor -> LargeCodeEditorDemoView()
        }
    }
}

fun generateRandomContent(size: Int): String {
    val random = Random
    return (0 ..< size).joinToString("") {
        when (val r = random.nextInt(26 + 26 + 10 + 4 + 1)) {
            in 0 ..< 26 -> 'A'.plus(r - 0)
            in 26 ..< 52 -> 'a'.plus(r - 26)
            in 52 ..< 62 -> '0'.plus(r - 52)
            in 62 ..< 66 -> ' '
            66 -> '\n'
            else -> throw RuntimeException("Unexpected random value: $r")
        }.toString()
    }
}
