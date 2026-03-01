package com.chvma.wordfight

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.chvma.wordfight.engine.GameEngine
import com.chvma.wordfight.speech.createSpeechEngine
import com.chvma.wordfight.ui.GameOverScreen
import com.chvma.wordfight.ui.GameScreen

@Composable
fun App() {
    val gameEngine = remember { GameEngine() }
    val speechEngine = remember { createSpeechEngine() }

    var isGameOver by remember { mutableStateOf(false) }
    var lastScore by remember { mutableIntStateOf(0) }
    var bestScore by remember { mutableIntStateOf(0) }

    if (isGameOver) {
        GameOverScreen(
            score = lastScore,
            bestScore = bestScore,
            onRestart = {
                gameEngine.restart()
                isGameOver = false
            }
        )
    } else {
        GameScreen(
            gameEngine = gameEngine,
            speechEngine = speechEngine,
            onGameOver = { score, best ->
                lastScore = score
                bestScore = best
                isGameOver = true
            }
        )
    }
}
