package com.chvma.wordfight.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.chvma.pronounceWord.R

@Composable
actual fun musicIconPainter(isOn: Boolean): Painter {
    return painterResource(if (isOn) R.drawable.ic_music_on else R.drawable.ic_music_off)
}