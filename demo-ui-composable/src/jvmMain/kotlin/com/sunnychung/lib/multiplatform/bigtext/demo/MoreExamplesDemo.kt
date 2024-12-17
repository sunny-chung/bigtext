package com.sunnychung.lib.multiplatform.bigtext.demo

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sunnychung.lib.multiplatform.bigtext.util.buildAnnotatedStringPatched
import com.sunnychung.lib.multiplatform.bigtext.ux.BigMonospaceTextField
import com.sunnychung.lib.multiplatform.bigtext.ux.rememberConcurrentLargeAnnotatedBigTextFieldState
import kotlin.random.Random

@Composable
fun MoreExamplesDemoView() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SingleLineDemoView()
        MouseHoverAnnotatedTextDemo()
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
            BigMonospaceTextField(
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
            BigMonospaceTextField(
                textFieldState = bigTextFieldState,
                color = Color.Black,
                cursorColor = Color.Blue,
                fontFamily = FontFamily.SansSerif,
                isSingleLineInput = true,
                onPointerEvent = { event, tag ->
                    log.d { "onPointerEvent: $tag" }
                    tooltip = tag
                },
                padding = PaddingValues(all = 8.dp),
                modifier = Modifier.background(Color(255, 192, 160), RoundedCornerShape(4.dp))
                    .fillMaxWidth()
            )
        }
    }
}
