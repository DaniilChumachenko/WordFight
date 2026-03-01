package com.chvma.wordfight.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.chvma.wordfight.engine.GameEngine
import com.chvma.wordfight.speech.SpeechEngine
import kotlinx.coroutines.isActive

@Composable
fun GameScreen(
    gameEngine: GameEngine,
    speechEngine: SpeechEngine,
    onGameOver: (score: Int, best: Int) -> Unit,
) {
    val state by gameEngine.state.collectAsState()

    var screenWidth by remember { mutableFloatStateOf(0f) }
    var screenHeight by remember { mutableFloatStateOf(0f) }

    // Game loop
    LaunchedEffect(Unit) {
        var lastTime = 0L
        while (isActive) {
            val time = withFrameMillis { it }
            val delta = if (lastTime == 0L) 0.016f else ((time - lastTime) / 1000f).coerceAtMost(0.1f)
            lastTime = time
            gameEngine.update(delta, screenWidth, screenHeight)
        }
    }

    // Speech recognition
    LaunchedEffect(Unit) {
        speechEngine.start("en-US")
        speechEngine.partialFlow.collect { spoken ->
            gameEngine.tryMatch(spoken)
        }
    }

    DisposableEffect(Unit) {
        onDispose { speechEngine.stop() }
    }

    // Game over trigger
    LaunchedEffect(state.isGameOver) {
        if (state.isGameOver) {
            onGameOver(state.score, state.bestScore)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
            .onSizeChanged {
                screenWidth = it.width.toFloat()
                screenHeight = it.height.toFloat()
            }
    ) {
        // Falling cards
        if (screenWidth > 0f && screenHeight > 0f) {
            state.activeCards.forEach { card ->
                FallingCard(
                    card = card,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                )
            }
        }

        // HUD
        HudOverlay(
            score = state.score,
            lives = state.lives,
            level = state.level,
            isPaused = state.isPaused,
            onPause = { gameEngine.pause() },
            onResume = { gameEngine.resume() },
            modifier = Modifier
                .fillMaxWidth()
                .safeContentPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}
