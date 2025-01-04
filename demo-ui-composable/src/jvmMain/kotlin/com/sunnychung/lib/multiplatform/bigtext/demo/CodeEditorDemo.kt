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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.sunnychung.lib.multiplatform.bigtext.core.BigText
import com.sunnychung.lib.multiplatform.bigtext.core.ConcurrentBigText
import com.sunnychung.lib.multiplatform.bigtext.util.emptyToNull
import com.sunnychung.lib.multiplatform.bigtext.util.string
import com.sunnychung.lib.multiplatform.bigtext.ux.BigTextField
import com.sunnychung.lib.multiplatform.bigtext.ux.BigTextFieldState
import com.sunnychung.lib.multiplatform.bigtext.ux.BigTextKeyboardInputProcessor
import com.sunnychung.lib.multiplatform.bigtext.ux.BigTextSimpleLayoutResult
import com.sunnychung.lib.multiplatform.bigtext.ux.BigTextViewState
import com.sunnychung.lib.multiplatform.bigtext.ux.createFromLargeAnnotatedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kotlincrypto.hash.sha2.SHA256

@Composable
fun CodeEditorDemoView() {
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
    var numOfLinesInitially by remember { mutableStateOf(0) }
    var isSoftWrapEnabled by remember { mutableStateOf(false) }
    var numOfComputations by remember { mutableStateOf(0) }
    val isLoading = numOfComputations > 0
    var isWaitForMeasure by remember { mutableStateOf(false) }
    var textHash by remember { mutableStateOf(calculateSha256Hash(bigTextFieldState.text)) }
    val scrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    val coroutineScope1 = rememberCoroutineScope()

    val isDisableSyntaxHighlighting = bigTextFieldState.text.length > 2 * 1024 * 1024 // 2 MB

    fun loadBigTextInBackground(contentKey: String) {
        ++numOfComputations
        bigTextFieldState = BigTextFieldState( // clear the text before loading
            ConcurrentBigText(BigText.createFromLargeAnnotatedString(AnnotatedString(""))),
            BigTextViewState()
        )
        textHash = ""
        coroutineScope1.launch {
            bigTextFieldState.viewState.isLayoutDisabled = true
            isWaitForMeasure = false

            val (textState, hash) = withContext(Dispatchers.IO) {
                val initialText = PRELOAD_CONTENT[contentKey]!!
                val textState = BigTextFieldState(
                    ConcurrentBigText(BigText.createFromLargeAnnotatedString(AnnotatedString(initialText))),
                    BigTextViewState()
                )
                textState to calculateSha256Hash(textState.text)
            }
            bigTextFieldState = textState
            textHash = hash
            numOfLinesInitially = textState.text.numOfLines
            scrollState.scrollTo(0)
            isWaitForMeasure = true
        }
    }

    LaunchedEffect(bigTextFieldState) {
        withContext(Dispatchers.IO) {
            bigTextFieldState.valueChangesFlow
                .debounce(1000L)
                .collect {
                    val hash = (bigTextFieldState.text as ConcurrentBigText).withReadLock { text ->
                        calculateSha256Hash(text)
                    }
                    withContext(Dispatchers.Default) {
                        textHash = hash
                    }
                }
        }
    }

    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            PRELOAD_CONTENT
                .filter { it.value.isEmpty() || it.value.length > 1 * 1024 }
                .keys
                .forEach { key ->
                    Button(
                        onClick = {
                            loadBigTextInBackground(key)
                        },
                        enabled = numOfComputations == 0
                    ) {
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
                        refTotalLines = numOfLinesInitially,
                        layoutResult = bigTextLayoutResult,
                        scrollState = scrollState,
                        onCorrectMeasured = {
                            if (isWaitForMeasure) {
                                log.d("onCorrectMeasured")
                                bigTextFieldState.viewState.isLayoutDisabled = false
                                --numOfComputations
                                isWaitForMeasure = false
                            }
                        },
                    )
                    Box {
                        BigTextField(
                            textFieldState = bigTextFieldState,
                            cursorColor = Color.Black,
                            textDecorator = if (isDisableSyntaxHighlighting) {
                                null
                            } else {
                                remember(bigTextFieldState) { DemoSyntaxHighlightDecorator() }
                            },
                            onTextChange = { textHash = "" },
                            scrollState = scrollState,
                            horizontalScrollState = horizontalScrollState,
                            onTextLayout = { bigTextLayoutResult = it },
                            isSoftWrapEnabled = isSoftWrapEnabled,
                            onHeavyComputation = { computation -> // compute in background and display a "loading" spinner
                                withContext(coroutineScope1.coroutineContext) {
                                    ++numOfComputations
                                    log.d { "numOfComputations = $numOfComputations" }
                                }
                                withContext(Dispatchers.IO) {
                                    log.d { "compute in IO" }
                                    computation()
                                }
                                withContext(coroutineScope1.coroutineContext) {
                                    --numOfComputations
                                    log.d { "numOfComputations = $numOfComputations" }
                                }
                            },
                            keyboardInputProcessor = object : BigTextKeyboardInputProcessor {
                                override fun beforeProcessInput(it: KeyEvent, viewState: BigTextViewState): Boolean {
                                    return if (it.type == KeyEventType.KeyDown && it.key == Key.Enter) {
                                        onPressEnterAddIndent(bigTextFieldState.text, bigTextFieldState.viewState)
                                        true
                                    } else {
                                        false
                                    }
                                }
                            },
                            modifier = Modifier.background(Color(224, 224, 224))
                                .fillMaxSize()
                        )
                        if (!isLoading) {
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

fun onPressEnterAddIndent(text: BigText, viewState: BigTextViewState) {
    val lineIndex = text.findLineAndColumnFromRenderPosition(viewState.cursorIndex).first
    val previousLineString = text.findLineString(lineIndex) // as '\n' is not yet inputted, current line is the "previous line"
    val spacesMatch = "^([ \t]+)".toRegex().matchAt(previousLineString, 0)
    val newSpaces = "\n" + (spacesMatch?.groups?.get(1)?.value ?: "")
    if (viewState.hasSelection()) {
        text.delete(viewState.selection)
    }
    text.insertAt(viewState.cursorIndex, newSpaces)
    viewState.setCursorIndex(viewState.cursorIndex + newSpaces.length)
    text.recordCurrentChangeSequenceIntoUndoHistory()
}

@OptIn(ExperimentalStdlibApi::class)
fun calculateSha256Hash(bigText: BigText): String {
    val digest = SHA256()
    val chunkSize = 2 * 1024 * 1024
    val length = bigText.length
    (0 .. length / chunkSize).forEach {
        val content = bigText.substring(it * chunkSize, ((it + 1) * chunkSize).coerceAtMost(length)).string()
        val bytes = content.encodeToByteArray()
        digest.update(bytes)
    }
    return digest.digest().toHexString()
}
