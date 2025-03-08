package com.sunnychung.lib.multiplatform.bigtext.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.sunnychung.lib.multiplatform.bigtext.core.BigText
import com.sunnychung.lib.multiplatform.bigtext.core.ConcurrentBigText
import com.sunnychung.lib.multiplatform.bigtext.platform.AsyncOperation
import com.sunnychung.lib.multiplatform.bigtext.ux.BigTextField
import com.sunnychung.lib.multiplatform.bigtext.ux.BigTextFieldState
import com.sunnychung.lib.multiplatform.bigtext.ux.BigTextViewState
import com.sunnychung.lib.multiplatform.bigtext.ux.createFromLargeAnnotatedString
import com.sunnychung.lib.multiplatform.bigtext.ux.createFromTinyAnnotatedString
import com.sunnychung.lib.multiplatform.bigtext.ux.rememberConcurrentLargeAnnotatedBigTextFieldState

@Composable
fun ManyTextFieldsDemo() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        (0.. 129).forEach { i ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                BigTextField(
                    modifier = Modifier.weight(4f).background(Color(red = .76f, green = 1f, blue = .76f)),
                    textFieldState = rememberConcurrentTinyAnnotatedBigTextFieldState(AnnotatedString("Key $i"), "k$i").component1(),
                    isSingleLineInput = true,
                    fontFamily = FontFamily.SansSerif,
                    cursorColor = Color.Black,
                    onHeavyComputation = AsyncOperation.Synchronous, // force immediate layout and rendering
                )
                BigTextField(
                    modifier = Modifier.weight(6f).background(Color(red = .76f, green = 1f, blue = .76f)),
                    textFieldState = rememberConcurrentTinyAnnotatedBigTextFieldState(AnnotatedString("Value $i"), "v$i").component1(),
                    isSingleLineInput = true,
                    fontFamily = FontFamily.SansSerif,
                    cursorColor = Color.Black,
                    onHeavyComputation = AsyncOperation.Synchronous, // force immediate layout and rendering
                )
            }
        }
    }
}

@Composable
fun rememberConcurrentTinyAnnotatedBigTextFieldState(initialValue: AnnotatedString, vararg cacheKeys: Any?, initialize: (BigTextFieldState) -> Unit = {}): MutableState<BigTextFieldState> {
    return rememberSaveable(*cacheKeys) {
        com.sunnychung.lib.multiplatform.bigtext.ux.log.i { "cache miss concurrent 1" }
        mutableStateOf(
            BigTextFieldState(
                ConcurrentBigText(BigText.createFromTinyAnnotatedString(initialValue)),
                BigTextViewState()
            ).apply(initialize)
        )
    }
}

