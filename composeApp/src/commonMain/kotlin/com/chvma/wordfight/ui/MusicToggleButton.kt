package com.chvma.wordfight.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MusicToggleButton(
    isEnabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onToggle, modifier = modifier) {
        Icon(
            painter = musicIconPainter(isEnabled),
            contentDescription = if (isEnabled) "Music on" else "Music off",
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}
