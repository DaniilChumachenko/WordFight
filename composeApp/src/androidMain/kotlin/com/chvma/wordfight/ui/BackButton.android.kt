package com.chvma.wordfight.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
actual fun BackButtonContent() {
    Text(
        text = "\uD83D\uDD19",
        color = Color.White,
        fontSize = 22.sp,
    )
}
