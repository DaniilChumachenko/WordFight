package com.chvma.wordfight.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.painterResource
import wordfight.composeapp.generated.resources.Res
import wordfight.composeapp.generated.resources.ic_music_off
import wordfight.composeapp.generated.resources.ic_music_on

@Composable
actual fun musicIconPainter(isOn: Boolean): Painter {
    return painterResource(if (isOn) Res.drawable.ic_music_on else Res.drawable.ic_music_off)
}
