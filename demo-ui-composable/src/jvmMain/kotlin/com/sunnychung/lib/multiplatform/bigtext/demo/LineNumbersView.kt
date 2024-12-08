package com.sunnychung.lib.multiplatform.bigtext.demo

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sunnychung.lib.multiplatform.bigtext.core.BigText
import com.sunnychung.lib.multiplatform.bigtext.core.transform.BigTextTransformed
import com.sunnychung.lib.multiplatform.bigtext.ux.BigTextSimpleLayoutResult
import com.sunnychung.lib.multiplatform.bigtext.ux.BigTextViewState
import kotlin.math.abs

@Composable
fun LineNumbersView(
    modifier: Modifier = Modifier,
    bigTextViewState: BigTextViewState,
    bigText: BigText,
    layoutResult: BigTextSimpleLayoutResult?,
    scrollState: ScrollState,
    onCorrectMeasured: () -> Unit,
) = with(LocalDensity.current) {
    val textStyle = LocalTextStyle.current.copy(
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        color = Color.DarkGray,
    )

    // Note that layoutResult.text != bigText
    val layoutText = layoutResult?.text as? BigTextTransformed

    val viewportTop = scrollState.value
    val visibleRows = bigTextViewState.calculateVisibleRowRange(viewportTop)
    log.v { "scroll = $viewportTop; visibleRows = $visibleRows; totalLines = ${layoutText?.numOfOriginalLines}" }
    val rowHeight = layoutResult?.rowHeight ?: 0f
    // Note: it is possible that `visibleRows.first` > `visibleRows.last`, when the text covered by this range is removed.

    val firstRow = visibleRows.first
    val lastRow = visibleRows.endInclusive + 1
    val rowToLineIndex = { it: Int -> layoutText?.findOriginalLineIndexByRowIndex(it) ?: 0 }
    val totalLines = bigText.numOfLines
    val lineHeight = (rowHeight).toDp()
    val getRowOffset = { it: Int ->
        (it * rowHeight - viewportTop).toDp()
    }

    val textMeasurer = rememberTextMeasurer()
    val lineNumDigits = "$totalLines".length
    val width = remember(lineNumDigits) {
        maxOf(textMeasurer.measure("8".repeat(lineNumDigits), textStyle, maxLines = 1).size.width.toDp(), 20.dp) +
                4.dp + 4.dp
    }

    Box(
        modifier = modifier
            .width(width)
            .fillMaxHeight()
            .clipToBounds()
            .background(Color.LightGray)
            .onGloballyPositioned { // need to be put before padding modifiers so that measured size includes padding
                if (abs(it.size.width - width.toPx()) < 0.1 /* equivalent to `it.size.width == width.toPx()` */) {
                    onCorrectMeasured()
                }
            }
            .padding(top = 6.dp, start = 4.dp, end = 4.dp)
    ) {
        var i: Int = firstRow
        var lastLineIndex = -1
        while (i < lastRow) {
            val lineIndex = rowToLineIndex(i)

            if (lineIndex > lastLineIndex) {
                Row(
                    modifier = Modifier
                        .height(lineHeight)
                        .offset(y = getRowOffset(i)),
                ) {
                    Box(
                        contentAlignment = Alignment.CenterEnd,
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = (rowToLineIndex(i) + 1).toString(),
                            style = textStyle,
                            maxLines = 1,
                        )
                    }
                }
            }
            lastLineIndex = lineIndex
            ++i
        }
    }
}
