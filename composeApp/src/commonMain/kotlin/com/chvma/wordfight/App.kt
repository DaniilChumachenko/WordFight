package com.chvma.wordfight

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.chvma.wordfight.audio.BgmTrack
import com.chvma.wordfight.audio.createBgmPlayer
import com.chvma.wordfight.engine.GameEngine
import com.chvma.wordfight.localization.AppLanguage
import com.chvma.wordfight.localization.Localization
import com.chvma.wordfight.model.WordContent
import com.chvma.wordfight.speech.createPermissionManager
import com.chvma.wordfight.speech.createSpeechEngine
import com.chvma.wordfight.storage.createSettingsStorage
import com.chvma.wordfight.storage.createWordStorage
import com.chvma.wordfight.ui.GameOverScreen
import com.chvma.wordfight.ui.GameScreen
import com.chvma.wordfight.ui.HomeScreen
import com.chvma.wordfight.ui.LanguageScreen
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
    object Languages : Screen()
}

@Composable
fun App() {
    val gameEngine = remember { GameEngine() }
    val speechEngine = remember { createSpeechEngine() }
    val permissionManager = remember { createPermissionManager() }
    val wordStorage = remember { createWordStorage() }
    val settingsStorage = remember { createSettingsStorage() }
    val bgmPlayer = remember { createBgmPlayer() }
    val scope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var lastScore by remember { mutableIntStateOf(0) }
    var bestScore by remember { mutableIntStateOf(0) }
    var missedWords by remember { mutableStateOf<List<WordContent>>(emptyList()) }
    var hasPermission by remember { mutableStateOf(false) }
    var isMenuMusicEnabled by remember { mutableStateOf(true) }
    var isGameMusicEnabled by remember { mutableStateOf(true) }
    var language by remember { mutableStateOf(AppLanguage.EN) }
    val strings = remember(language) { Localization.strings(language) }

    LaunchedEffect(Unit) {
        bestScore = withContext(Dispatchers.Default) {
            wordStorage.getBestScore()
        }
        hasPermission = withContext(Dispatchers.Default) {
            permissionManager.hasPermission()
        }
        language = withContext(Dispatchers.Default) {
            settingsStorage.getLanguage()
        }
    }

    LaunchedEffect(currentScreen, isMenuMusicEnabled, isGameMusicEnabled) {
        val isGame = currentScreen is Screen.Game
        val enabled = if (isGame) isGameMusicEnabled else isMenuMusicEnabled
        if (!enabled) {
            bgmPlayer.stop()
            return@LaunchedEffect
        }
        val track = if (isGame) BgmTrack.Game else BgmTrack.Menu
        bgmPlayer.startLoop(track)
    }

    DisposableEffect(Unit) {
        onDispose { bgmPlayer.stop() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            is Screen.Home -> {
                HomeScreen(
                    onStartGame = {
                        currentScreen = Screen.Game
                    },
                    onMyWords = {
                        currentScreen = Screen.MyWords
                    },
                    onLanguages = {
                        currentScreen = Screen.Languages
                    },
                    hasPermission = hasPermission,
                    onPermissionGranted = {
                        scope.launch(Dispatchers.Default) {
                            val granted = permissionManager.hasPermission()
                            hasPermission = granted
                        }
                    },
                    musicEnabled = isMenuMusicEnabled,
                    onToggleMusic = { isMenuMusicEnabled = !isMenuMusicEnabled },
                    strings = strings,
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
                    musicEnabled = isGameMusicEnabled,
                    onToggleMusic = { isGameMusicEnabled = !isGameMusicEnabled },
                    language = language,
                    strings = strings,
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
                    musicEnabled = isMenuMusicEnabled,
                    onToggleMusic = { isMenuMusicEnabled = !isMenuMusicEnabled },
                    language = language,
                    strings = strings,
                )
            }
            is Screen.MyWords -> {
                MyWordsScreen(
                    onBack = {
                        currentScreen = Screen.Home
                    },
                    musicEnabled = isMenuMusicEnabled,
                    onToggleMusic = { isMenuMusicEnabled = !isMenuMusicEnabled },
                    language = language,
                    strings = strings,
                )
            }
            is Screen.Languages -> {
                LanguageScreen(
                    current = language,
                    onSelect = { selected ->
                        language = selected
                        scope.launch(Dispatchers.Default) {
                            settingsStorage.setLanguage(selected)
                        }
                    },
                    onBack = {
                        currentScreen = Screen.Home
                    },
                    musicEnabled = isMenuMusicEnabled,
                    onToggleMusic = { isMenuMusicEnabled = !isMenuMusicEnabled },
                    strings = strings,
                )
            }
        }
    }
}
