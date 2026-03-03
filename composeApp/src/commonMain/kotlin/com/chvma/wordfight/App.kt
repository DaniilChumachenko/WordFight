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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.chvma.wordfight.ads.createInterstitialAdManager
import com.chvma.wordfight.ads.createRewardedAdManager
import com.chvma.wordfight.audio.BgmTrack
import com.chvma.wordfight.audio.createBgmPlayer
import com.chvma.wordfight.engine.GameEngine
import com.chvma.wordfight.leaderboard.LeaderboardEntry
import com.chvma.wordfight.leaderboard.LeaderboardPeriod
import com.chvma.wordfight.leaderboard.createLeaderboardRepository
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
import com.chvma.wordfight.ui.LeaderboardScreen
import com.chvma.wordfight.ui.MyWordsScreen
import com.chvma.wordfight.ui.PlayerOnboardingDialog
import com.chvma.wordfight.ui.theme.WordFightTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class Screen {
    object Home : Screen()
    object Game : Screen()
    object GameOver : Screen()
    object MyWords : Screen()
    object Languages : Screen()
    object Leaderboard : Screen()
}

@Composable
fun App(isSdkReady: Boolean = true) {
    val gameEngine = remember { GameEngine() }
    val speechEngine = remember { createSpeechEngine() }
    val permissionManager = remember { createPermissionManager() }
    val wordStorage = remember { createWordStorage() }
    val settingsStorage = remember { createSettingsStorage() }
    val leaderboardRepository = remember { createLeaderboardRepository(settingsStorage) }
    val interstitialAdManager = remember { createInterstitialAdManager() }
    val rewardedAdManager = remember { createRewardedAdManager() }
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
    var myWordsTransitions by remember { mutableIntStateOf(0) }
    var isAppInForeground by remember { mutableStateOf(true) }

    var showOnboarding by remember { mutableStateOf(false) }
    var onboardingName by remember { mutableStateOf("") }
    var onboardingLanguage by remember { mutableStateOf(AppLanguage.EN) }

    var leaderboardPeriod by remember { mutableStateOf(LeaderboardPeriod.TODAY) }
    var leaderboardEntries by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var isLeaderboardLoading by remember { mutableStateOf(false) }

    val strings = remember(language) { Localization.strings(language) }
    val onboardingStrings = remember(onboardingLanguage) { Localization.strings(onboardingLanguage) }
    val lifecycleOwner = LocalLifecycleOwner.current

    fun loadLeaderboard(period: LeaderboardPeriod) {
        leaderboardPeriod = period
        scope.launch {
            isLeaderboardLoading = true
            leaderboardEntries = withContext(Dispatchers.Default) {
                leaderboardRepository.getLeaderboardWindow(period)
            }
            isLeaderboardLoading = false
        }
    }

    LaunchedEffect(Unit) {
        data class StartupState(
            val bestScore: Int,
            val hasPermission: Boolean,
            val language: AppLanguage,
            val onboardingCompleted: Boolean,
            val onboardingName: String,
        )

        val startupState = withContext(Dispatchers.Default) {
            val loadedBestScore = wordStorage.getBestScore()
            leaderboardRepository.syncAllTimeBaseline(loadedBestScore)
            StartupState(
                bestScore = loadedBestScore,
                hasPermission = permissionManager.hasPermission(),
                language = settingsStorage.getLanguage(),
                onboardingCompleted = settingsStorage.isOnboardingCompleted(),
                onboardingName = settingsStorage.getPlayerName().orEmpty(),
            )
        }

        bestScore = startupState.bestScore
        hasPermission = startupState.hasPermission
        language = startupState.language
        onboardingLanguage = startupState.language
        onboardingName = startupState.onboardingName
        showOnboarding = !startupState.onboardingCompleted || startupState.onboardingName.isBlank()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> isAppInForeground = true
                Lifecycle.Event.ON_STOP -> isAppInForeground = false
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(currentScreen, isMenuMusicEnabled, isGameMusicEnabled, isAppInForeground) {
        val isGame = currentScreen is Screen.Game
        val enabled = if (isGame) isGameMusicEnabled else isMenuMusicEnabled
        if (!enabled || !isAppInForeground) {
            bgmPlayer.pause()
            return@LaunchedEffect
        }
        val track = if (isGame) BgmTrack.Game else BgmTrack.Menu
        bgmPlayer.startLoop(track)
    }

    LaunchedEffect(isSdkReady) {
        if (isSdkReady) {
            interstitialAdManager.loadAd()
            rewardedAdManager.loadAd()
        }
    }

    DisposableEffect(Unit) {
        onDispose { bgmPlayer.stop() }
    }

    val navigateToMyWords = {
        myWordsTransitions += 1
        val shouldShowInterstitial = myWordsTransitions % 5 == 0
        if (isSdkReady && shouldShowInterstitial) {
            interstitialAdManager.showAd {
                currentScreen = Screen.MyWords
            }
        } else {
            currentScreen = Screen.MyWords
        }
    }

    WordFightTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                is Screen.Home -> {
                    HomeScreen(
                        onStartGame = {
                            currentScreen = Screen.Game
                        },
                        onMyWords = navigateToMyWords,
                        onLanguages = {
                            currentScreen = Screen.Languages
                        },
                        onLeaderboard = {
                            loadLeaderboard(LeaderboardPeriod.TODAY)
                            currentScreen = Screen.Leaderboard
                        },
                        hasPermission = hasPermission,
                        onPermissionGranted = {
                            scope.launch {
                                val granted = withContext(Dispatchers.Default) {
                                    permissionManager.hasPermission()
                                }
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
                            val updatedBest = maxOf(bestScore, best, score)
                            bestScore = updatedBest
                            missedWords = gameEngine.getMissedWords()

                            scope.launch(Dispatchers.Default) {
                                wordStorage.saveBestScore(updatedBest)
                                leaderboardRepository.submitGameScore(score)
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
                        onPauseWithAd = { onPaused ->
                            if (isSdkReady) {
                                rewardedAdManager.showAd { _ ->
                                    onPaused()
                                }
                            } else {
                                onPaused()
                            }
                        },
                        onReviveWithAd = { onResult ->
                            if (isSdkReady) {
                                rewardedAdManager.showAd { rewarded ->
                                    onResult(rewarded)
                                }
                            } else {
                                onResult(false)
                            }
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

                is Screen.Leaderboard -> {
                    LeaderboardScreen(
                        selectedPeriod = leaderboardPeriod,
                        entries = leaderboardEntries,
                        loading = isLeaderboardLoading,
                        onSelectPeriod = { period ->
                            loadLeaderboard(period)
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

            if (showOnboarding) {
                PlayerOnboardingDialog(
                    name = onboardingName,
                    selectedLanguage = onboardingLanguage,
                    onNameChange = { onboardingName = it },
                    onLanguageSelect = { selected -> onboardingLanguage = selected },
                    onConfirm = {
                        val finalName = onboardingName.trim()
                        if (finalName.isEmpty()) return@PlayerOnboardingDialog
                        val selectedLanguage = onboardingLanguage

                        scope.launch {
                            withContext(Dispatchers.Default) {
                                settingsStorage.ensurePlayerId()
                                settingsStorage.setPlayerName(finalName)
                                settingsStorage.setLanguage(selectedLanguage)
                                settingsStorage.setOnboardingCompleted(true)
                            }
                            language = selectedLanguage
                            showOnboarding = false
                        }
                    },
                    strings = onboardingStrings,
                )
            }
        }
    }
}
