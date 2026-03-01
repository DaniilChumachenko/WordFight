package com.chvma.wordfight.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

@Composable
expect fun musicIconPainter(isOn: Boolean): Painter
