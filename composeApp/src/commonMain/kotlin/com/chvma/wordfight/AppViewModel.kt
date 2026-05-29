package com.chvma.wordfight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chvma.wordfight.ads.AdUnitIds
import com.chvma.wordfight.ads.RewardedAdManager
import com.chvma.wordfight.ads.createInterstitialAdManager
import com.chvma.wordfight.audio.BgmTrack
import com.chvma.wordfight.audio.createBgmPlayer
import com.chvma.wordfight.engine.GameEngine
import com.chvma.wordfight.leaderboard.LeaderboardPeriod
import com.chvma.wordfight.leaderboard.createLeaderboardRepository
import com.chvma.wordfight.localization.AppLanguage
import com.chvma.wordfight.speech.createPermissionManager
import com.chvma.wordfight.speech.createSpeechEngine
import com.chvma.wordfight.storage.createSettingsStorage
import com.chvma.wordfight.storage.createWordStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Owns all global app state, business logic and long-lived engines/repositories
 * that used to live inside the `App` composable. Screens stay stateless and only
 * receive snapshots of [uiState] plus callbacks into this view model.
 */
class AppViewModel : ViewModel() {

    // Engines consumed directly by the gameplay screen.
    val gameEngine = GameEngine()
    val speechEngine = createSpeechEngine()

    private val permissionManager = createPermissionManager()
    private val wordStorage = createWordStorage()
    private val settingsStorage = createSettingsStorage()
    private val leaderboardRepository = createLeaderboardRepository(settingsStorage)
    private val interstitialAdManager = createInterstitialAdManager()
    private val rewardedPauseAdManager = RewardedAdManager(AdUnitIds.rewardedPause)
    private val rewardedExtraLifeAdManager = RewardedAdManager(AdUnitIds.rewardedExtraLife)
    private val bgmPlayer = createBgmPlayer()

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private var isSdkReady = false
    private var adsLoaded = false
    private var myWordsTransitions = 0
    private var leaderboardTransitions = 0

    /** Loads persisted settings and dismisses the splash once ready. */
    fun onStart() {
        viewModelScope.launch {
            val startup = withContext(Dispatchers.Default) {
                val best = wordStorage.getBestScore()
                leaderboardRepository.syncAllTimeBaseline(best)
                StartupData(
                    bestScore = best,
                    hasPermission = permissionManager.hasPermission(),
                    language = settingsStorage.getLanguage(),
                    onboardingCompleted = settingsStorage.isOnboardingCompleted(),
                    playerName = settingsStorage.getPlayerName().orEmpty(),
                )
            }
            _uiState.update {
                it.copy(
                    bestScore = startup.bestScore,
                    hasPermission = startup.hasPermission,
                    language = startup.language,
                    isPlayerRegistered = startup.onboardingCompleted && startup.playerName.isNotBlank(),
                )
            }
            delay(SPLASH_HOLD_MILLIS)
            _uiState.update { it.copy(isSplashVisible = false) }
        }
    }

    fun setSdkReady(ready: Boolean) {
        isSdkReady = ready
        if (ready && !adsLoaded) {
            adsLoaded = true
            interstitialAdManager.loadAd()
            rewardedPauseAdManager.loadAd()
            rewardedExtraLifeAdManager.loadAd()
        }
    }

    fun refreshPermission() {
        viewModelScope.launch {
            val granted = withContext(Dispatchers.Default) { permissionManager.hasPermission() }
            _uiState.update { it.copy(hasPermission = granted) }
        }
    }

    fun toggleMenuMusic() = _uiState.update { it.copy(isMenuMusicEnabled = !it.isMenuMusicEnabled) }

    fun toggleGameMusic() = _uiState.update { it.copy(isGameMusicEnabled = !it.isGameMusicEnabled) }

    fun setLanguage(language: AppLanguage) {
        _uiState.update { it.copy(language = language) }
        viewModelScope.launch(Dispatchers.Default) { settingsStorage.setLanguage(language) }
    }

    fun restartGame() = gameEngine.restart()

    fun onGameOver(score: Int, best: Int) {
        val updatedBest = maxOf(_uiState.value.bestScore, best, score)
        val missed = gameEngine.getMissedWords()
        _uiState.update { it.copy(lastScore = score, bestScore = updatedBest, missedWords = missed) }
        viewModelScope.launch(Dispatchers.Default) {
            wordStorage.saveBestScore(updatedBest)
            leaderboardRepository.submitGameScore(score)
        }
    }

    fun loadLeaderboard(period: LeaderboardPeriod) {
        _uiState.update { it.copy(leaderboard = it.leaderboard.copy(period = period, isLoading = true)) }
        viewModelScope.launch {
            val entries = withContext(Dispatchers.Default) {
                leaderboardRepository.getLeaderboardWindow(period)
            }
            _uiState.update {
                it.copy(leaderboard = it.leaderboard.copy(period = period, entries = entries, isLoading = false))
            }
        }
    }

    /** Opens "My words", showing an interstitial on every [AD_FREQUENCY]-th visit. */
    fun openMyWords(onNavigate: () -> Unit) {
        myWordsTransitions += 1
        if (isSdkReady && myWordsTransitions % AD_FREQUENCY == 0) {
            interstitialAdManager.showAd(onNavigate)
        } else {
            onNavigate()
        }
    }

    /**
     * Opens the leaderboard. First-time visitors without a name are routed to the
     * onboarding screen; registered players go straight to the table (ad-gated).
     */
    fun openLeaderboard(onOnboarding: () -> Unit, onLeaderboard: () -> Unit) {
        if (!_uiState.value.isPlayerRegistered) {
            onOnboarding()
            return
        }
        leaderboardTransitions += 1
        val open = {
            loadLeaderboard(LeaderboardPeriod.TODAY)
            onLeaderboard()
        }
        if (isSdkReady && leaderboardTransitions % AD_FREQUENCY == 0) {
            interstitialAdManager.showAd { open() }
        } else {
            open()
        }
    }

    /** Persists the player profile, pushes the best achievement, then continues. */
    fun confirmOnboarding(name: String, language: AppLanguage, onDone: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                settingsStorage.ensurePlayerId()
                settingsStorage.setPlayerName(name)
                settingsStorage.setLanguage(language)
                settingsStorage.setOnboardingCompleted(true)
                // Push the best achievement earned before registration into the rating.
                leaderboardRepository.submitBestScores()
            }
            _uiState.update { it.copy(language = language, isPlayerRegistered = true) }
            loadLeaderboard(LeaderboardPeriod.TODAY)
            onDone()
        }
    }

    fun showPauseAd(onResult: (rewarded: Boolean) -> Unit) {
        if (isSdkReady) rewardedPauseAdManager.showAd(onResult) else onResult(true)
    }

    fun showReviveAd(onResult: (rewarded: Boolean) -> Unit) {
        if (isSdkReady) rewardedExtraLifeAdManager.showAd(onResult) else onResult(false)
    }

    /** Drives background music based on the active screen, focus and splash state. */
    fun updateBgm(isGameScreen: Boolean, isForeground: Boolean) {
        val state = _uiState.value
        val enabled = if (isGameScreen) state.isGameMusicEnabled else state.isMenuMusicEnabled
        if (state.isSplashVisible || !enabled || !isForeground) {
            bgmPlayer.pause()
            return
        }
        bgmPlayer.startLoop(if (isGameScreen) BgmTrack.Game else BgmTrack.Menu)
    }

    override fun onCleared() {
        bgmPlayer.stop()
        super.onCleared()
    }

    private data class StartupData(
        val bestScore: Int,
        val hasPermission: Boolean,
        val language: AppLanguage,
        val onboardingCompleted: Boolean,
        val playerName: String,
    )

    private companion object {
        const val AD_FREQUENCY = 5
        const val SPLASH_HOLD_MILLIS = 500L
    }
}
