package com.sunnychung.lib.multiplatform.bigtext.demo

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sunnychung.application.multiplatform.hellohttp.ux.bigtext.BigMonospaceTextField
import com.sunnychung.application.multiplatform.hellohttp.ux.bigtext.BigText
import com.sunnychung.application.multiplatform.hellohttp.ux.bigtext.BigTextTransformer
import com.sunnychung.application.multiplatform.hellohttp.ux.bigtext.IncrementalTextTransformation
import com.sunnychung.application.multiplatform.hellohttp.ux.bigtext.rememberConcurrentLargeAnnotatedBigTextFieldState
import kotlin.random.Random

val PRELOAD_CONTENT = mapOf(
    "Empty" to "",
    "1 KB" to generateRandomContent(1 * 1024),
    "10 KB" to generateRandomContent(10 * 1024),
    "1 MB" to generateRandomContent(1 * 1024 * 1024),
    "10 MB" to generateRandomContent(10 * 1024 * 1024),
)

@Composable
fun AppView() {
    var cacheKey by remember { mutableStateOf(0) }
    var generateContentKey by remember { mutableStateOf("Empty") }

    val bigTextFieldState by rememberConcurrentLargeAnnotatedBigTextFieldState(PRELOAD_CONTENT[generateContentKey]!!, cacheKey)

    Column(Modifier.padding(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PRELOAD_CONTENT.keys.forEach { key ->
                Button(onClick = {
                    generateContentKey = key
                    ++cacheKey
                }) {
                    Text(text = if (key != "Empty") "Random $key" else key)
                }
            }
        }

        Box {
            val scrollState = rememberScrollState()

            BigMonospaceTextField(
                textFieldState = bigTextFieldState,
                color = Color.Black,
                cursorColor = Color.Blue,
                scrollState = scrollState,
                modifier = Modifier.background(Color(224, 224, 160))
                    .fillMaxSize()
            )
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(scrollState),
                modifier = Modifier.align(Alignment.TopEnd).fillMaxHeight()
            )
        }
    }
}

fun generateRandomContent(size: Int): String {
    val random = Random
    return (0 ..< size).joinToString("") {
        when (val r = random.nextInt(26 + 26 + 10 + 4 + 1)) {
            in 0 ..< 26 -> 'A'.plus(r - 0)
            in 26 ..< 52 -> 'a'.plus(r - 26)
            in 52 ..< 62 -> '0'.plus(r - 26)
            in 62 ..< 66 -> ' '
            66 -> '\n'
            else -> throw RuntimeException("Unexpected random value: $r")
        }.toString()
    }
}
