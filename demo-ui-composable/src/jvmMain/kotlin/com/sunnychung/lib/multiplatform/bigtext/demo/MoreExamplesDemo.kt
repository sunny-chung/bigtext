package com.sunnychung.lib.multiplatform.bigtext.demo

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sunnychung.lib.multiplatform.bigtext.core.BigText
import com.sunnychung.lib.multiplatform.bigtext.core.ConcurrentBigText
import com.sunnychung.lib.multiplatform.bigtext.util.buildAnnotatedStringPatched
import com.sunnychung.lib.multiplatform.bigtext.ux.BigTextField
import com.sunnychung.lib.multiplatform.bigtext.ux.BigTextFieldState
import com.sunnychung.lib.multiplatform.bigtext.ux.BigTextViewState
import com.sunnychung.lib.multiplatform.bigtext.ux.createFromLargeAnnotatedString
import com.sunnychung.lib.multiplatform.bigtext.ux.rememberConcurrentLargeAnnotatedBigTextFieldState

@Composable
fun MoreExamplesDemoView() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SingleLineDemoView()
        MouseHoverAnnotatedTextDemo()
        SharedStateDemoView()
    }
}

@Composable
private fun SingleLineDemoView() {
    val bigTextFieldState by rememberConcurrentLargeAnnotatedBigTextFieldState()
    val horizontalScrollState = rememberScrollState()

    var enterStatusText by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Single Line + Key Events", fontSize = 16.sp)
        Text("Try to type something long and then hit enter.")

        Box {
            BigTextField(
                textFieldState = bigTextFieldState,
                color = Color.Black,
                cursorColor = Color.Blue,
                fontFamily = FontFamily.Serif,
                isSingleLineInput = true,
                horizontalScrollState = horizontalScrollState,
                padding = PaddingValues(all = 8.dp),
                modifier = Modifier.background(Color(255, 192, 160), RoundedCornerShape(4.dp))
                    .fillMaxWidth()
                    .onKeyEvent {
                        if (it.key == Key.Enter) {
                            enterStatusText = "${bigTextFieldState.text.length} char entered."
                            true
                        } else {
                            false
                        }
                    }
            )
            HorizontalScrollbar(
                adapter = rememberScrollbarAdapter(horizontalScrollState),
                modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth()
            )
        }

        Text(enterStatusText)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MouseHoverAnnotatedTextDemo() {
    val ANNOTATAED_STYLE = SpanStyle(background = Color.Yellow, color = Color.Blue)

    // Note: Do NOT use buildAnnotatedString, or the tags would be gone.
    val initialText = buildAnnotatedStringPatched {
        fun appendWithTag(text: String, tag: String) {
            append(AnnotatedString(text, listOf(AnnotatedString.Range(ANNOTATAED_STYLE, 0, text.length, tag))))
        }

        append("https://")
        appendWithTag("{domain}", "github.com")
        append("/")
        appendWithTag("{user}", "sunny-chung")
        append("/bigtext")
    }

    val bigTextFieldState by rememberConcurrentLargeAnnotatedBigTextFieldState(initialText)
    var tooltip by remember { mutableStateOf<String?>(null) }
    var statusText by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Mouse Events", fontSize = 16.sp)

        TooltipArea(
            tooltip = {
                val tooltip = tooltip
                if (!tooltip.isNullOrEmpty()) {
                    Text(
                        text = tooltip,
                        color = Color(32, 128, 32),
                        modifier = Modifier.background(Color(192, 255, 192), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(32, 128, 32), RoundedCornerShape(6.dp))
                            .padding(6.dp)
                    )
                }
            },
            delayMillis = 0,
        ) {
            BigTextField(
                textFieldState = bigTextFieldState,
                color = Color.Black,
                cursorColor = Color.Blue,
                fontFamily = FontFamily.SansSerif,
                isSingleLineInput = true,
                onPointerEvent = { event, tag ->
                    log.d { "onPointerEvent: $tag" }
                    tooltip = tag
                    if (event.type == PointerEventType.Press) {
                        if (tag != null) {
                            statusText = "Clicked '$tag'."
                        } else {
                            statusText = ""
                        }
                    }
                },
                padding = PaddingValues(all = 8.dp),
                modifier = Modifier.background(Color(255, 192, 160), RoundedCornerShape(4.dp))
                    .fillMaxWidth()
            )
        }

        Text(statusText)
    }
}

@Composable
private fun SharedStateDemoView() {
    val text = remember { ConcurrentBigText(BigText.createFromLargeAnnotatedString(AnnotatedString("Top Content\n\n# Section Header 1\n\nContent 1.1\n`Content 1.2`\nContent 1.3\n\n# Section Header 2\n\nCon*tent* 2.1\n_Conten_t 2.2\n\n# Section Header 3\n\n~Content 3.1~\nContent 3.2\nContent 3.3\nContent 3.4\nContent 3.5\nContent 3.6\n"))) }
    val bigTextFieldState1 by remember { mutableStateOf(BigTextFieldState(text, BigTextViewState())) }
    val bigTextFieldState2 by remember { mutableStateOf(BigTextFieldState(text, BigTextViewState())) }
    val scrollState = rememberScrollState()

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Shared BigText State and Incremental-offset Transformation", fontSize = 16.sp)

        Row(Modifier.fillMaxWidth()) {
            Box(Modifier.weight(1f)) {
                BigTextField(
                    textFieldState = bigTextFieldState1,
                    color = Color.Black,
                    cursorColor = Color.Blue,
                    fontFamily = FontFamily.Monospace,
                    scrollState = scrollState,
                    padding = PaddingValues(all = 8.dp),
                    modifier = Modifier.background(Color(255, 192, 160), RoundedCornerShape(4.dp))
                        .fillMaxSize()
                )
                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(scrollState),
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
                )
            }

            Box(Modifier.weight(1f)) {
                BigTextField(
                    textFieldState = bigTextFieldState2,
                    color = Color.Black,
                    cursorColor = Color.Blue,
                    fontFamily = FontFamily.SansSerif,
                    scrollState = scrollState,
                    textTransformation = remember { SimpleMarkdownSlowTransformation() },
                    padding = PaddingValues(all = 8.dp),
                    modifier = Modifier.background(Color(240, 240, 240), RoundedCornerShape(4.dp))
                        .fillMaxSize()
                )
                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(scrollState),
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
                )
            }
        }
    }
}

