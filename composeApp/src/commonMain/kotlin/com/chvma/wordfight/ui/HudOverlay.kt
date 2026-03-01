package com.chvma.wordfight.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HudOverlay(
    score: Int,
    lives: Int,
    level: Int,
    isPaused: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onExit: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Lives
        Text(
            modifier = Modifier.weight(1f),
            text = "❤️".repeat(lives.coerceIn(0, 5)),
            fontSize = 20.sp,
        )

        // Score + Level
        Text(
            modifier = Modifier.padding(end = 10.dp),
            text = "Score: $score",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )

        // Controls
        // Pause/Resume
        Text(
            modifier =
                Modifier
                    .background(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(vertical = 6.dp, horizontal = 10.dp)
                    .clickable{
                        if (isPaused) onResume() else onPause()
                    },
            text = if (isPaused) "▶" else "⏸",
            color = Color.White,
            fontSize = 22.sp,
        )

        Spacer(Modifier.width(6.dp))

        Text(
            modifier = Modifier
                .background(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(vertical = 6.dp, horizontal = 10.dp)
                .clickable{onExit()},
            text = "✕",
            color = Color(0xFFFF5252).copy(alpha = 0.8f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
