package com.sunnychung.lib.multiplatform.bigtext.demo

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.LocalTextStyle
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun DebugView() {
    var generateContentKey by remember { mutableStateOf("Empty") }

    var isSoftWrapEnabled by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            PRELOAD_CONTENT
                .keys
                .forEach { key ->
                    Button(onClick = {
                        generateContentKey = key
                        coroutineScope.launch {
                            scrollState.scrollTo(0)
                        }
                    }) {
                        Text(text = key)
                    }
                }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { isSoftWrapEnabled = !isSoftWrapEnabled }
                .padding(end = 16.dp)
        ) {
            Checkbox(checked = isSoftWrapEnabled, onCheckedChange = { isSoftWrapEnabled = it })
            Text("Soft Wrap")
        }

        Box(Modifier.fillMaxSize()) {
//            Column(modifier = Modifier
//                .fillMaxSize()
//                .verticalScroll(scrollState)
//                .run {
//                    if (isSoftWrapEnabled) {
//                        this
//                    } else {
//                        horizontalScroll(horizontalScrollState)
//                    }
//                }
//            ) {
                BasicTextField(
                    value = PRELOAD_CONTENT[generateContentKey]!!,
                    onValueChange = {},
                    textStyle = LocalTextStyle.current.copy(
                        color = Color.Black,
                        fontFamily = FontFamily.Monospace,
                    ),
                    modifier = Modifier
                        .background(Color(224, 224, 160))
                        .fillMaxSize()
                        .padding(4.dp)
                )
//            }
//            VerticalScrollbar(
//                adapter = rememberScrollbarAdapter(scrollState),
//                modifier = Modifier.align(Alignment.TopEnd).fillMaxHeight()
//            )
//            if (!isSoftWrapEnabled) {
//                HorizontalScrollbar(
//                    adapter = rememberScrollbarAdapter(horizontalScrollState),
//                    modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth()
//                )
//            }
        }
    }
}
