package com.chvma.wordfight.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun HudOverlay(
    score: Int,
    lives: Int,
    level: Int,
    isPaused: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Lives
        Text(
            text = "❤️".repeat(lives.coerceIn(0, 5)),
            fontSize = 20.sp,
        )

        // Score + Level
        Text(
            text = "Score: $score  Lvl: $level",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )

        // Pause/Resume
        Button(
            onClick = if (isPaused) onResume else onPause,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
        ) {
            Text(
                text = if (isPaused) "▶" else "⏸",
                color = Color.White,
                fontSize = 18.sp,
            )
        }
    }
}
