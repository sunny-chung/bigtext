package com.sunnychung.lib.multiplatform.bigtext.demo

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
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
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.sunnychung.lib.multiplatform.bigtext.core.BigText
import com.sunnychung.lib.multiplatform.bigtext.core.ConcurrentBigText
import com.sunnychung.lib.multiplatform.bigtext.util.emptyToNull
import com.sunnychung.lib.multiplatform.bigtext.ux.BigMonospaceTextField
import com.sunnychung.lib.multiplatform.bigtext.ux.BigTextFieldState
import com.sunnychung.lib.multiplatform.bigtext.ux.BigTextSimpleLayoutResult
import com.sunnychung.lib.multiplatform.bigtext.ux.BigTextViewState
import com.sunnychung.lib.multiplatform.bigtext.ux.createFromLargeAnnotatedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kotlincrypto.hash.sha2.SHA256

@Composable
fun LargeCodeEditorDemoView() {
    var bigTextFieldState by remember {
        mutableStateOf(
            BigTextFieldState(
                text = ConcurrentBigText(
                    BigText.createFromLargeAnnotatedString(
                        AnnotatedString("")
                    )
                ),
                viewState = BigTextViewState()
            )
        )
    }
    var bigTextLayoutResult by remember { mutableStateOf<BigTextSimpleLayoutResult?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isWaitForMeasure by remember { mutableStateOf(false) }
    var textHash by remember { mutableStateOf(calculateSha256Hash("")) }
    val scrollState = rememberScrollState()
    val coroutineScope1 = rememberCoroutineScope()

    val isDisableSyntaxHighlighting = bigTextFieldState.text.length > 2 * 1024 * 1024 // 2 MB

    fun loadBigTextInBackground(contentKey: String) {
        coroutineScope1.launch {
            isLoading = true
            bigTextFieldState.viewState.isLayoutDisabled = true
            isWaitForMeasure = false

            val (textState, hash) = withContext(Dispatchers.IO) {
                val initialText = PRELOAD_CONTENT[contentKey]!!
                BigTextFieldState(
                    ConcurrentBigText(BigText.createFromLargeAnnotatedString(AnnotatedString(initialText))),
                    BigTextViewState()
                ) to calculateSha256Hash(initialText)
            }
            bigTextFieldState = textState
            textHash = hash
            scrollState.scrollTo(0)
            isWaitForMeasure = true
        }
    }

    LaunchedEffect(bigTextFieldState) {
        withContext(Dispatchers.IO) {
            bigTextFieldState.valueChangesFlow
                .debounce(1000L)
                .collect {
                    val fullString = bigTextFieldState.text.buildString()
                    val hash = calculateSha256Hash(fullString)
                    withContext(Dispatchers.Default) {
                        textHash = hash
                    }
                }
        }
    }

    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PRELOAD_CONTENT
                .filter { it.value.isEmpty() || it.value.length > 1 * 1024 }
                .keys
                .forEach { key ->
                    Button(onClick = {
                        loadBigTextInBackground(key)
                    }) {
                        Text(text = if (key != "Empty") "Random $key" else key)
                    }
                }
        }

        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize())  {
                if (isDisableSyntaxHighlighting) {
                    Text(
                        text = "Syntax highlighting is disabled over 2 MB.",
                        modifier = Modifier.background(Color(1f, 0.6f, 0.6f))
                            .fillMaxWidth()
                            .padding(6.dp)
                    )
                }
                Row(Modifier.weight(1f)) {
                    LineNumbersView(
                        bigTextViewState = bigTextFieldState.viewState,
                        bigText = bigTextFieldState.text,
                        layoutResult = bigTextLayoutResult,
                        scrollState = scrollState,
                        onCorrectMeasured = {
                            if (isWaitForMeasure) {
                                log.d("onCorrectMeasured")
                                bigTextFieldState.viewState.isLayoutDisabled = false
                                isLoading = false
                                isWaitForMeasure = false
                            }
                        },
                    )
                    if (!isLoading) {
                        Box {
                            BigMonospaceTextField(
                                textFieldState = bigTextFieldState,
                                cursorColor = Color.Black,
                                textDecorator = if (isDisableSyntaxHighlighting) {
                                    null
                                } else {
                                    remember(bigTextFieldState) { DemoSyntaxHighlightDecorator() }
                                },
                                onTextChange = { textHash = "" },
                                scrollState = scrollState,
                                onTextLayout = { bigTextLayoutResult = it },
                                modifier = Modifier.background(Color(224, 224, 224))
                                    .fillMaxSize()
                            )
                            VerticalScrollbar(
                                adapter = rememberScrollbarAdapter(scrollState),
                                modifier = Modifier.align(Alignment.TopEnd).fillMaxHeight()
                            )
                        }
                    }
                }
                Text(
                    text = "Size: ${bigTextFieldState.text.length} chars; SHA-256: ${textHash.emptyToNull() ?: "(Calculating)"}",
                    modifier = Modifier.background(Color.LightGray)
                        .fillMaxWidth()
                        .padding(6.dp)
                )
            }
            if (isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
fun calculateSha256Hash(content: String): String {
    val bytes = content.encodeToByteArray()
    val digest = SHA256()
    digest.update(bytes)
    return digest.digest().toHexString()
}
