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
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.CircularProgressIndicator
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
import com.sunnychung.lib.multiplatform.bigtext.core.BigText
import com.sunnychung.lib.multiplatform.bigtext.core.createFromLargeString
import com.sunnychung.lib.multiplatform.bigtext.ux.BigTextLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ReadOnlyDemoView() {
    var bigText by remember { mutableStateOf<BigText>(BigText.createFromLargeString("")) }
    var isLoading by remember { mutableStateOf(false) }
    var isSoftWrapEnabled by remember { mutableStateOf(false) }
    var isSelectable by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    fun loadBigTextInBackground(contentKey: String) {
        coroutineScope.launch {
            isLoading = true

            val text = withContext(Dispatchers.IO) {
                val initialText = PRELOAD_CONTENT[contentKey]!!
                BigText.createFromLargeString(initialText)
            }
            bigText = text
            scrollState.scrollTo(0)

            isLoading = false
        }
    }

    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            PRELOAD_CONTENT
                .keys
                .forEach { key ->
                    Button(onClick = {
                        loadBigTextInBackground(key)
                    }) {
                        Text(text = key)
                    }
                }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { isSoftWrapEnabled = !isSoftWrapEnabled }
                    .padding(end = 16.dp)
            ) {
                Checkbox(checked = isSoftWrapEnabled, onCheckedChange = { isSoftWrapEnabled = it })
                Text("Soft Wrap")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { isSelectable = !isSelectable }
                    .padding(end = 16.dp)
            ) {
                Checkbox(checked = isSelectable, onCheckedChange = { isSelectable = it })
                Text("Selectable")
            }
        }

        Box(Modifier.fillMaxSize().background(Color(192, 255, 192))) {
            if (!isLoading) {
                BigTextLabel(
                    text = bigText,
                    color = Color.Black,
                    isSoftWrapEnabled = isSoftWrapEnabled,
                    isSelectable = isSelectable,
                    textDecorator = remember(bigText) { DemoSyntaxHighlightDecorator() },
                    scrollState = scrollState,
                    horizontalScrollState = horizontalScrollState, // only required for soft wrap disabled
                    modifier = Modifier.fillMaxSize()
                )
                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(scrollState),
                    modifier = Modifier.align(Alignment.TopEnd).fillMaxHeight()
                )
                if (!isSoftWrapEnabled) {
                    HorizontalScrollbar(
                        adapter = rememberScrollbarAdapter(horizontalScrollState),
                        modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth()
                    )
                }
            } else {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
    }
}
