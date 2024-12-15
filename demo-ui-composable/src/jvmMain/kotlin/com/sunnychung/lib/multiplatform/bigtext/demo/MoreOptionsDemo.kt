package com.sunnychung.lib.multiplatform.bigtext.demo

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sunnychung.lib.multiplatform.bigtext.ux.BigMonospaceTextField
import com.sunnychung.lib.multiplatform.bigtext.ux.rememberConcurrentLargeAnnotatedBigTextFieldState

@Composable
fun MoreOptionsDemoView() {
    var cacheKey by remember { mutableStateOf(0) }
    var generateContentKey by remember { mutableStateOf("Empty") }

    val bigTextFieldState by rememberConcurrentLargeAnnotatedBigTextFieldState()
    val horizontalScrollState = rememberScrollState()

    var enterStatusText by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Single Line", fontSize = 16.sp)

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
