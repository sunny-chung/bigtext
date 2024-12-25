package com.sunnychung.lib.multiplatform.bigtext.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Composable
fun MixedFontDemoView() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TransformationTextAreaDemoView(Modifier.weight(1f), bodyFontFamily = FontFamily.Serif)
    }
}
