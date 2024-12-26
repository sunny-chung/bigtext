package com.sunnychung.lib.multiplatform.bigtext.demo

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.sunnychung.lib.multiplatform.bigtext.util.hexToUtf8String
import kotlin.random.Random

val PRELOAD_CONTENT = linkedMapOf(
    "Empty" to "",
    "1 KB" to generateRandomContent(1 * 1024),
    "10 KB" to generateRandomContent(10 * 1024),
    "1 MB" to generateRandomContent(1 * 1024 * 1024),
    "10 MB" to generateRandomContent(10 * 1024 * 1024),
    "100 MB" to generateRandomContent(100 * 1024 * 1024),
    "Unicode 4 KB" to generateRandomUnicodeContent(4 * 1024),
    "Unicode 10 MB" to generateRandomUnicodeContent(10 * 1024 * 1024),
    "Unicode 100 MB" to generateRandomUnicodeContent(100 * 1024 * 1024),
    "Unicode + Emoji 4 KB" to generateRandomUnicodeWithEmojiContent(4 * 1024),
    "Unicode + Emoji 10 MB" to generateRandomUnicodeWithEmojiContent(10 * 1024 * 1024),
    "Dense 10 MB" to generateDenseRandomContent(10 * 1024 * 1024),
    "Single Line 10 MB" to generateSingleLongLine(10 * 1024 * 1024),
)

private enum class DemoView(val displayName: String) {
    SimpleMonospace("Simple Monospace"),
    CodeEditor("Code Editor"),
    SansSerif("Sans Serif"),
    Transformation("Transformation"),
    MixedFont("Multiple Fonts"),
    ReadOnly("Read-only"),
    MoreExamples("More Examples"),
    ComposeText("Compare with Jetpack Compose BasicTextField"),
    ComposeText2("Compare with Jetpack Compose BasicTextField2"),
}

@Composable
fun AppView() {
//    return DebugView()

    var chosenDemoView by remember { mutableStateOf(DemoView.SimpleMonospace) }

    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            DemoView
                .entries
                .forEach {
                    Button(
                        onClick = {
                            chosenDemoView = it
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor =
                            if (it == chosenDemoView) Color.Green
                            else Color.Cyan
                        )
                    ) {
                        Text(text = it.displayName)
                    }
                }
        }
        when (chosenDemoView) {
            DemoView.SimpleMonospace -> SimpleDemoView(fontFamily = FontFamily.Monospace)
            DemoView.CodeEditor -> CodeEditorDemoView()
            DemoView.SansSerif -> SimpleDemoView(fontFamily = FontFamily.SansSerif)
            DemoView.Transformation -> TransformationDemoView()
            DemoView.MixedFont -> MixedFontDemoView()
            DemoView.ReadOnly -> ReadOnlyDemoView()
            DemoView.MoreExamples -> MoreExamplesDemoView()
            DemoView.ComposeText -> ComposeTextFieldView()
            DemoView.ComposeText2 -> ComposeTextField2View()
        }
    }
}

fun generateRandomContent(size: Int): String {
    val random = Random
    return (0 ..< size).joinToString("") {
        when (val r = random.nextInt(26 + 26 + 10 + 4 + 1)) {
            in 0 ..< 26 -> 'A'.plus(r - 0)
            in 26 ..< 52 -> 'a'.plus(r - 26)
            in 52 ..< 62 -> '0'.plus(r - 52)
            in 62 ..< 66 -> ' '
            66 -> '\n'
            else -> throw RuntimeException("Unexpected random value: $r")
        }.toString()
    }
}

fun generateRandomUnicodeContent(size: Int): String {
    val random = Random
    return (0 ..< size).joinToString("") {
        when (val r = random.nextInt(240)) {
            in 0 ..< 26 -> 'A'.plus(r - 0)
            in 26 ..< 52 -> 'a'.plus(r - 26)
            in 52 ..< 62 -> '0'.plus(r - 52)
            in 62 ..< 67 -> ' '
            in 67 ..< 70 -> '\n'
            in 70 ..< 100 -> '\u2FAC'.plus(r - 70) // Traditional Chinese
            in 100 ..< 130 -> '\u3041'.plus(r - 100) // Japanese
            in 130 ..< 160 -> '\uAC00'.plus(r - 130) // Korean
            in 160 ..< 180 -> hexToUtf8String(0x20f2f + (r - 160)) // Multi-byte Unicode
            in 180 ..< 200 -> hexToUtf8String(0x22b5 + (r - 180)) // Symbol
            in 200 ..< 215 -> hexToUtf8String(0x4e07 + (r - 200)) // Simplified Chinese
            in 215 ..< 235 -> hexToUtf8String(0x0100 + (r - 215)) // European Latin extended
            in 235 ..< 265 -> "!@#$%^&*()-=_+[]{}:;\"',./<>?|\\"[r - 235] // Punctuation
            in 265 ..< 275 -> "π©¥º…≈≤£¢∞"[r - 265] // Symbol

            else -> throw RuntimeException("Unexpected random value: $r")
        }.toString()
    }
}

fun generateRandomUnicodeWithEmojiContent(size: Int): String {
    val random = Random
    return (0 ..< size).joinToString("") {
        when (val r = random.nextInt(240)) {
            in 0 ..< 26 -> 'A'.plus(r - 0)
            in 26 ..< 52 -> 'a'.plus(r - 26)
            in 52 ..< 62 -> '0'.plus(r - 52)
            in 62 ..< 66 -> ' '
            in 66 ..< 69 -> '\n'
            69 -> "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC67" // Emoji Sequence
            in 70 ..< 100 -> '\u2FAC'.plus(r - 70) // Traditional Chinese
            in 100 ..< 130 -> '\u3041'.plus(r - 100) // Japanese
            in 130 ..< 160 -> '\uAC00'.plus(r - 130) // Korean
            in 160 ..< 180 -> hexToUtf8String(0x20f2f + (r - 160)) // Multi-byte Unicode
            in 180 ..< 200 -> hexToUtf8String(0x22b5 + (r - 180)) // Symbol
            in 200 ..< 210 -> hexToUtf8String(0x1f315 + (r - 200)) // Emoji
            in 210 ..< 218 -> hexToUtf8String(0x1f929 + (r - 210)) // Emoji
            in 218 ..< 220 -> hexToUtf8String(0x231a + (r - 218)) // Emoji
            in 220 ..< 225 -> hexToUtf8String(0x1f44b) + hexToUtf8String(0x1f3fb + (r - 220)) // Emoji Modifier Sequence
            in 225 ..< 240 -> hexToUtf8String(0x4e07 + (r - 225)) // Simplified Chinese
            in 240 ..< 260 -> hexToUtf8String(0x0100 + (r - 240)) // European Latin extended
            in 260 ..< 290 -> "!@#$%^&*()-=_+[]{}:;\"',./<>?|\\"[r - 260] // Punctuation
            in 290 ..< 300 -> "π©¥º…≈≤£¢∞"[r - 290] // Symbol

            else -> throw RuntimeException("Unexpected random value: $r")
        }.toString()
    }
}

fun generateDenseRandomContent(size: Int): String {
    val random = Random
    return (0 ..< size).joinToString("") {
        if (random.nextInt(100000) < 1) {
            "\n"
        } else {
            when (val r = random.nextInt(64)) {
                in 0 ..< 26 -> 'A'.plus(r - 0)
                in 26 ..< 52 -> 'a'.plus(r - 26)
                in 52 ..< 62 -> '0'.plus(r - 52)
                in 62 ..< 64 -> ' '
                else -> throw RuntimeException("Unexpected random value: $r")
            }.toString()
        }
    }
}

fun generateSingleLongLine(size: Int): String {
    val random = Random
    return (0 ..< size).joinToString("") {
        when (val r = random.nextInt(64)) {
            in 0 ..< 26 -> 'A'.plus(r - 0)
            in 26 ..< 52 -> 'a'.plus(r - 26)
            in 52 ..< 62 -> '0'.plus(r - 52)
            in 62 ..< 64 -> ' '
            else -> throw RuntimeException("Unexpected random value: $r")
        }.toString()
    }
}

fun generateSingleLongLineWithoutSpace(size: Int): String {
    val random = Random
    return (0 ..< size).joinToString("") {
        when (val r = random.nextInt(62)) {
            in 0 ..< 26 -> 'A'.plus(r - 0)
            in 26 ..< 52 -> 'a'.plus(r - 26)
            in 52 ..< 62 -> '0'.plus(r - 52)
            else -> throw RuntimeException("Unexpected random value: $r")
        }.toString()
    }
}
