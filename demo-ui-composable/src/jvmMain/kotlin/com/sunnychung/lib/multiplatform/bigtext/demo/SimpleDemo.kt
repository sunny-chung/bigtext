package com.sunnychung.lib.multiplatform.bigtext.demo

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sunnychung.lib.multiplatform.bigtext.ux.BigMonospaceTextField
import com.sunnychung.lib.multiplatform.bigtext.ux.rememberConcurrentLargeAnnotatedBigTextFieldState
import kotlinx.coroutines.launch

@Composable
fun SimpleDemoView() {
    var cacheKey by remember { mutableStateOf(0) }
    var generateContentKey by remember { mutableStateOf("Empty") }

    val bigTextFieldState by rememberConcurrentLargeAnnotatedBigTextFieldState(PRELOAD_CONTENT[generateContentKey]!!, cacheKey)
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PRELOAD_CONTENT
                .filter { it.value.length <= 32 * 1024 * 1024 }
                .keys
                .forEach { key ->
                    Button(onClick = {
                        generateContentKey = key
                        ++cacheKey
                        coroutineScope.launch {
                            scrollState.scrollTo(0)
                        }
                    }) {
                        Text(text = if (key != "Empty") "Random $key" else key)
                    }
                }
        }

        Box {
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