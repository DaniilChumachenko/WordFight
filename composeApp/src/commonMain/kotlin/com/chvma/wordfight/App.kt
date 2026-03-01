package com.chvma.wordfight

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.chvma.wordfight.engine.GameEngine
import com.chvma.wordfight.model.WordContent
import com.chvma.wordfight.speech.createPermissionManager
import com.chvma.wordfight.speech.createSpeechEngine
import com.chvma.wordfight.storage.createWordStorage
import com.chvma.wordfight.ui.GameOverScreen
import com.chvma.wordfight.ui.GameScreen
import com.chvma.wordfight.ui.HomeScreen
import com.chvma.wordfight.ui.MyWordsScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope

sealed class Screen {
    object Home : Screen()
    object Game : Screen()
    object GameOver : Screen()
    object MyWords : Screen()
}

@Composable
fun App() {
    val gameEngine = remember { GameEngine() }
    val speechEngine = remember { createSpeechEngine() }
    val permissionManager = remember { createPermissionManager() }
    val wordStorage = remember { createWordStorage() }
    val scope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var lastScore by remember { mutableIntStateOf(0) }
    var bestScore by remember { mutableIntStateOf(0) }
    var missedWords by remember { mutableStateOf<List<WordContent>>(emptyList()) }
    var hasPermission by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        bestScore = withContext(Dispatchers.Default) {
            wordStorage.getBestScore()
        }
        hasPermission = withContext(Dispatchers.Default) {
            permissionManager.hasPermission()
        }
    }

    when (currentScreen) {
        is Screen.Home -> {
            HomeScreen(
                onStartGame = {
                    currentScreen = Screen.Game
                },
                onMyWords = {
                    currentScreen = Screen.MyWords
                },
                hasPermission = hasPermission,
                onPermissionGranted = {
                    scope.launch(Dispatchers.Default) {
                        val granted = permissionManager.hasPermission()
                        hasPermission = granted
                    }
                },
            )
        }
        is Screen.Game -> {
            GameScreen(
                gameEngine = gameEngine,
                speechEngine = speechEngine,
                onGameOver = { score, best ->
                    lastScore = score
                    bestScore = best
                    missedWords = gameEngine.getMissedWords()
                    if (score > bestScore) {
                        scope.launch(Dispatchers.Default) {
                            wordStorage.saveBestScore(score)
                        }
                    }
                    currentScreen = Screen.GameOver
                },
                onBack = {
                    gameEngine.restart()
                    currentScreen = Screen.Home
                },
            )
        }
        is Screen.GameOver -> {
            GameOverScreen(
                score = lastScore,
                bestScore = bestScore,
                missedWords = missedWords,
                onRestart = {
                    gameEngine.restart()
                    currentScreen = Screen.Game
                },
                onHome = {
                    gameEngine.restart()
                    currentScreen = Screen.Home
                },
            )
        }
        is Screen.MyWords -> {
            MyWordsScreen(
                onBack = {
                    currentScreen = Screen.Home
                },
            )
        }
    }
}
