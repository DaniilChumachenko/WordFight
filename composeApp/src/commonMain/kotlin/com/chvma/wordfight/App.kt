package com.chvma.wordfight

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.chvma.wordfight.localization.Localization
import com.chvma.wordfight.ui.AppSplashScreen
import com.chvma.wordfight.ui.CategoryScreen
import com.chvma.wordfight.ui.GameOverScreen
import com.chvma.wordfight.ui.GameScreen
import com.chvma.wordfight.ui.HomeScreen
import com.chvma.wordfight.ui.LanguageScreen
import com.chvma.wordfight.ui.LeaderboardScreen
import com.chvma.wordfight.ui.MyWordsScreen
import com.chvma.wordfight.ui.PlayerOnboardingScreen
import com.chvma.wordfight.ui.theme.WordFightTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

private object Routes {
    const val HOME = "home"
    const val GAME = "game"
    const val GAME_OVER = "gameOver"
    const val MY_WORDS = "myWords"
    const val CATEGORIES = "categories"
    const val LANGUAGES = "languages"
    const val LEADERBOARD = "leaderboard"
    const val ONBOARDING = "onboarding"
}

@Composable
fun App(isSdkReady: Boolean = true) {
    val viewModel: AppViewModel = viewModel { AppViewModel() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val strings = remember(uiState.language) { Localization.strings(uiState.language) }

    val lifecycleOwner = LocalLifecycleOwner.current
    var isForeground by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) { viewModel.onStart() }
    LaunchedEffect(isSdkReady) { viewModel.setSdkReady(isSdkReady) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> isForeground = true
                Lifecycle.Event.ON_STOP -> isForeground = false
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    LaunchedEffect(
        currentRoute,
        uiState.isMenuMusicEnabled,
        uiState.isGameMusicEnabled,
        isForeground,
        uiState.isSplashVisible,
    ) {
        viewModel.updateBgm(isGameScreen = currentRoute == Routes.GAME, isForeground = isForeground)
    }

    WordFightTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isSplashVisible) {
                AppSplashScreen(
                    title = strings.appTitle,
                    loadingText = strings.leaderboardLoading,
                )
            } else {
                NavHost(navController = navController, startDestination = Routes.HOME) {
                    composable(Routes.HOME) {
                        HomeScreen(
                            bestScore = uiState.bestScore,
                            onStartGame = { viewModel.startFullGame { navController.navigate(Routes.GAME) } },
                            onCategories = { navController.navigate(Routes.CATEGORIES) },
                            onMyWords = { viewModel.openMyWords { navController.navigate(Routes.MY_WORDS) } },
                            onLanguages = { navController.navigate(Routes.LANGUAGES) },
                            onLeaderboard = {
                                viewModel.openLeaderboard(
                                    onOnboarding = { navController.navigate(Routes.ONBOARDING) },
                                    onLeaderboard = { navController.navigate(Routes.LEADERBOARD) },
                                )
                            },
                            hasPermission = uiState.hasPermission,
                            onPermissionGranted = { viewModel.refreshPermission() },
                            musicEnabled = uiState.isMenuMusicEnabled,
                            onToggleMusic = { viewModel.toggleMenuMusic() },
                            strings = strings,
                        )
                    }

                    composable(Routes.GAME) {
                        GameScreen(
                            gameEngine = viewModel.gameEngine,
                            speechEngine = viewModel.speechEngine,
                            onGameOver = { score, best ->
                                viewModel.onGameOver(score, best)
                                navController.navigate(Routes.GAME_OVER) {
                                    popUpTo(Routes.GAME) { inclusive = true }
                                }
                            },
                            onBack = {
                                viewModel.restartGame()
                                navController.popBackStack()
                            },
                            musicEnabled = uiState.isGameMusicEnabled,
                            onToggleMusic = { viewModel.toggleGameMusic() },
                            language = uiState.language,
                            strings = strings,
                            showBannerAd = isSdkReady,
                            onPauseWithAd = { onResult -> viewModel.showPauseAd(onResult) },
                            onReviveWithAd = { onResult -> viewModel.showReviveAd(onResult) },
                        )
                    }

                    composable(Routes.GAME_OVER) {
                        GameOverScreen(
                            score = uiState.lastScore,
                            bestScore = uiState.bestScore,
                            missedWords = uiState.missedWords,
                            isRanked = uiState.lastGameRanked,
                            won = uiState.lastGameWon,
                            sessionLabel = uiState.lastSessionLabel,
                            onRestart = {
                                viewModel.restartGame()
                                navController.navigate(Routes.GAME) {
                                    popUpTo(Routes.GAME_OVER) { inclusive = true }
                                }
                            },
                            onHome = {
                                viewModel.restartGame()
                                navController.popBackStack(Routes.HOME, inclusive = false)
                            },
                            musicEnabled = uiState.isMenuMusicEnabled,
                            onToggleMusic = { viewModel.toggleMenuMusic() },
                            language = uiState.language,
                            strings = strings,
                        )
                    }

                    composable(Routes.MY_WORDS) {
                        MyWordsScreen(
                            onBack = { navController.popBackStack() },
                            onPlay = {
                                viewModel.startSavedWordsGame {
                                    navController.navigate(Routes.GAME)
                                }
                            },
                            musicEnabled = uiState.isMenuMusicEnabled,
                            onToggleMusic = { viewModel.toggleMenuMusic() },
                            language = uiState.language,
                            strings = strings,
                        )
                    }

                    composable(Routes.CATEGORIES) {
                        CategoryScreen(
                            onBack = { navController.popBackStack() },
                            onSelect = { category, level ->
                                viewModel.startCategoryGame(category, level) {
                                    navController.navigate(Routes.GAME)
                                }
                            },
                            musicEnabled = uiState.isMenuMusicEnabled,
                            onToggleMusic = { viewModel.toggleMenuMusic() },
                            language = uiState.language,
                            strings = strings,
                        )
                    }

                    composable(Routes.LANGUAGES) {
                        LanguageScreen(
                            current = uiState.language,
                            onSelect = { viewModel.setLanguage(it) },
                            onBack = { navController.popBackStack() },
                            musicEnabled = uiState.isMenuMusicEnabled,
                            onToggleMusic = { viewModel.toggleMenuMusic() },
                            strings = strings,
                        )
                    }

                    composable(Routes.ONBOARDING) {
                        PlayerOnboardingScreen(
                            initialLanguage = uiState.language,
                            onConfirm = { name, selectedLanguage ->
                                viewModel.confirmOnboarding(name, selectedLanguage) {
                                    navController.navigate(Routes.LEADERBOARD) {
                                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                                    }
                                }
                            },
                            onBack = { navController.popBackStack() },
                            musicEnabled = uiState.isMenuMusicEnabled,
                            onToggleMusic = { viewModel.toggleMenuMusic() },
                        )
                    }

                    composable(Routes.LEADERBOARD) {
                        LeaderboardScreen(
                            selectedPeriod = uiState.leaderboard.period,
                            entries = uiState.leaderboard.entries,
                            loading = uiState.leaderboard.isLoading,
                            onSelectPeriod = { viewModel.loadLeaderboard(it) },
                            onBack = { navController.popBackStack() },
                            musicEnabled = uiState.isMenuMusicEnabled,
                            onToggleMusic = { viewModel.toggleMenuMusic() },
                            strings = strings,
                        )
                    }
                }
            }
        }
    }
}
