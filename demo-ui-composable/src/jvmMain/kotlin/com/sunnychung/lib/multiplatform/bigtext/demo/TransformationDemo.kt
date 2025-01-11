package com.sunnychung.lib.multiplatform.bigtext.demo

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.sunnychung.lib.multiplatform.bigtext.extension.length
import com.sunnychung.lib.multiplatform.bigtext.ux.BigTextField
import com.sunnychung.lib.multiplatform.bigtext.ux.extra.PasswordIncrementalTransformation
import com.sunnychung.lib.multiplatform.bigtext.ux.rememberConcurrentLargeAnnotatedBigTextFieldState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

val TRANSFORMATION_PRELOAD_CONTENT = PRELOAD_CONTENT
    .filter { it.value.length <= 32 * 1024 * 1024 }
    .map { (key, content) ->
        val transformationCount = content.length / 300 // approximate a transformation every 300 B

        val random = Random
        val transformations = (0 ..< transformationCount).map {
            random.nextInt(0, content.length) to generateSingleLongLineWithoutSpace(random.nextInt(1, 21))
        }.sortedBy { it.first }

        key to buildString {
            var last = 0

            transformations.forEach {
                var start = it.first
                if (content[start].isLowSurrogate()) {
                    ++start // never insert transformation between high surrogate and low surrogate
                }
                if (start > last) {
                    append(content.substring(last ..< start))
                }
                append("\${{${it.second}}}")
                last = start
            }
            if (last < content.length) {
                append(content.substring(last))
            }
        }
    }
    .toMap(linkedMapOf())

@Composable
fun TransformationDemoView() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TransformationTextAreaDemoView(Modifier.weight(1f), bodyFontFamily = FontFamily.Monospace)

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Bottom) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                val bigTextFieldState by rememberConcurrentLargeAnnotatedBigTextFieldState()

                Text("Phone Number (Transformation + Input Filter + Single Line + Max Input Length)")

                BigTextField(
                    textFieldState = bigTextFieldState,
                    color = Color.Black,
                    cursorColor = Color.Blue,
                    fontFamily = FontFamily.SansSerif,
                    isSingleLineInput = true,
                    maxInputLength = 3 + 4 + 4,
                    textTransformation = remember { PhoneNumberIncrementalTransformation() },
                    inputFilter = { it.replace("[^0-9]".toRegex(), "") },
                    padding = PaddingValues(all = 8.dp),
                    modifier = Modifier.background(Color(192, 192, 255), RoundedCornerShape(4.dp))
                        .fillMaxWidth()
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                val bigTextFieldState by rememberConcurrentLargeAnnotatedBigTextFieldState()

                Text("Password")

                BigTextField(
                    textFieldState = bigTextFieldState,
                    color = Color.Black,
                    cursorColor = Color.Blue,
                    fontFamily = FontFamily.SansSerif,
                    isSingleLineInput = true,
                    textTransformation = remember { PasswordIncrementalTransformation() },
                    padding = PaddingValues(all = 8.dp),
                    modifier = Modifier.background(Color(160, 160, 160), RoundedCornerShape(4.dp))
                        .fillMaxWidth()
                )
            }
        }
    }

}

@Composable
fun TransformationTextAreaDemoView(modifier: Modifier, bodyFontFamily: FontFamily) {
    var cacheKey by remember { mutableStateOf(0) }
    var generateContentKey by remember { mutableStateOf("Empty") }

    val bigTextFieldState by rememberConcurrentLargeAnnotatedBigTextFieldState(TRANSFORMATION_PRELOAD_CONTENT[generateContentKey]!!, cacheKey)
    var isSoftWrapEnabled by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var numOfComputations by remember { mutableStateOf(0) }

    val text = bigTextFieldState.text
    val transformation = remember(bigTextFieldState) {
        VariableIncrementalTransformation()
    }

    val isTransformButtonEnabled =
            bigTextFieldState.viewState.selection.length in 1 .. 20 &&
            !bigTextFieldState.text.substring(bigTextFieldState.viewState.selection).contains("[\${} ]".toRegex())

    val focusRequester = remember { FocusRequester() }

    Column(modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            TRANSFORMATION_PRELOAD_CONTENT
                .keys
                .forEach { key ->
                    Button(
                        onClick = {
                            generateContentKey = key
                            ++cacheKey
                            coroutineScope.launch {
                                scrollState.scrollTo(0)
                            }
                        },
                        enabled = numOfComputations == 0
                    ) {
                        Text(text = key)
                    }
                }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { isSoftWrapEnabled = !isSoftWrapEnabled }
                    .padding(end = 16.dp)
            ) {
                Checkbox(checked = isSoftWrapEnabled, onCheckedChange = { isSoftWrapEnabled = it })
                Text("Soft Wrap")
            }

            Button(
                onClick = {
                    val selection = bigTextFieldState.viewState.selection
                    text.insertAt(selection.last + 1, "}}")
                    text.insertAt(selection.first, "\${{")
                    text.recordCurrentChangeSequenceIntoUndoHistory()

                    focusRequester.requestFocus()
                },
                enabled = isTransformButtonEnabled
            ) {
                Text("Transform")
            }
        }

        Text("Type \${{...}} to create a transformation. Alternatively, select a word within 20 characters and click the Transform button.")

        Spacer(Modifier.height(8.dp))

        Box {
            BigTextField(
                textFieldState = bigTextFieldState,
                color = Color.Black,
                cursorColor = Color.Blue,
                fontFamily = bodyFontFamily,
                isSoftWrapEnabled = isSoftWrapEnabled,
                textTransformation = transformation,
                onHeavyComputation = { computation -> // compute in background and display a "loading" spinner
                    withContext(coroutineScope.coroutineContext) {
                        ++numOfComputations
                    }
                    withContext(Dispatchers.IO) {
                        computation()
                    }
                    withContext(coroutineScope.coroutineContext) {
                        --numOfComputations
                    }
                },
                scrollState = scrollState,
                horizontalScrollState = horizontalScrollState, // only required for soft wrap disabled
                modifier = Modifier.background(Color(224, 224, 224))
                    .fillMaxSize()
                    .focusRequester(focusRequester)
            )
            if (numOfComputations > 0) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
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
            }
        }
    }
}
