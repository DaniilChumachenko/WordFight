package com.chvma.wordfight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chvma.wordfight.ads.AdUnitIds
import com.chvma.wordfight.ads.RewardedAdManager
import com.chvma.wordfight.ads.createInterstitialAdManager
import com.chvma.wordfight.audio.BgmTrack
import com.chvma.wordfight.content.WordRepository
import com.chvma.wordfight.audio.createBgmPlayer
import com.chvma.wordfight.engine.GameEngine
import com.chvma.wordfight.leaderboard.LeaderboardPeriod
import com.chvma.wordfight.leaderboard.createLeaderboardRepository
import com.chvma.wordfight.localization.AppLanguage
import com.chvma.wordfight.localization.Localization
import com.chvma.wordfight.model.WordCategory
import com.chvma.wordfight.speech.createPermissionManager
import com.chvma.wordfight.speech.createSpeechEngine
import com.chvma.wordfight.storage.createSettingsStorage
import com.chvma.wordfight.storage.createWordStorage
import kotlin.coroutines.cancellation.CancellationException
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

    // Only full-catalogue games (main menu / "all topics, all difficulty") count
    // toward the rating. Topic/difficulty/saved-words sessions are excluded.
    private var currentGameRanked = true

    // Human-readable description of the current finite session (topic + difficulty
    // or "my words"); null for ranked/full games. Shown on the results screen.
    private var currentSessionLabel: String? = null

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

    /**
     * Starts a fresh game over the full word catalogue and then navigates via
     * [onReady]. Use this for the main "tap to start" flow.
     */
    fun startFullGame(onReady: () -> Unit) {
        currentGameRanked = true
        currentSessionLabel = null
        gameEngine.setWordPool(WordRepository.words, limited = false)
        gameEngine.restart()
        onReady()
    }

    /**
     * Starts a fresh game restricted to the player's saved words (review mode),
     * then navigates via [onReady]. No-op when there are no saved words yet.
     */
    fun startSavedWordsGame(onReady: () -> Unit) {
        viewModelScope.launch {
            val saved = withContext(Dispatchers.Default) { wordStorage.getAllWords() }
            if (saved.isEmpty()) return@launch
            currentGameRanked = false
            currentSessionLabel = Localization.strings(_uiState.value.language).myWords
            gameEngine.setWordPool(saved, limited = true)
            gameEngine.restart()
            onReady()
        }
    }

    /**
     * Starts a fresh game restricted to a topic and/or difficulty, then navigates
     * via [onReady]. [category] = null means all topics, [level] = null means all
     * difficulties. No-op if the resulting pool is empty.
     */
    fun startCategoryGame(category: WordCategory?, level: Int?, onReady: () -> Unit) {
        val pool = WordRepository.wordsFor(category, level)
        if (pool.isEmpty()) return
        // "All topics" with no difficulty filter == the full catalogue → ranked,
        // endless. Any narrower selection is a finite, unranked session.
        val isFullCatalogue = category == null && level == null
        currentGameRanked = isFullCatalogue
        currentSessionLabel = if (isFullCatalogue) null else sessionLabel(category, level)
        gameEngine.setWordPool(pool, limited = !isFullCatalogue)
        gameEngine.restart()
        onReady()
    }

    /** "Topic · ⭐⭐"-style label for a finite session, in the current language. */
    private fun sessionLabel(category: WordCategory?, level: Int?): String {
        val language = _uiState.value.language
        val strings = Localization.strings(language)
        val topic = if (category == null) strings.allTopics else Localization.categoryName(category, language)
        val difficulty = level?.let { " · " + "⭐".repeat(it) } ?: ""
        return topic + difficulty
    }

    fun restartGame() = gameEngine.restart()

    fun onGameOver(score: Int, best: Int) {
        val missed = gameEngine.getMissedWords()
        if (!currentGameRanked) {
            // Topic / difficulty / saved-words session — show results but keep it
            // out of the rating and personal best.
            val won = gameEngine.state.value.won
            _uiState.update {
                it.copy(
                    lastScore = score,
                    lastGameRanked = false,
                    lastGameWon = won,
                    lastSessionLabel = currentSessionLabel.orEmpty(),
                    missedWords = missed,
                )
            }
            return
        }
        val updatedBest = maxOf(_uiState.value.bestScore, score)
        _uiState.update {
            it.copy(
                lastScore = score,
                lastGameRanked = true,
                lastGameWon = false,
                bestScore = updatedBest,
                missedWords = missed,
            )
        }
        viewModelScope.launch(Dispatchers.Default) {
            wordStorage.saveBestScore(updatedBest)
            leaderboardRepository.submitGameScore(score)
        }
    }

    fun loadLeaderboard(period: LeaderboardPeriod) {
        _uiState.update { it.copy(leaderboard = it.leaderboard.copy(period = period, isLoading = true)) }
        viewModelScope.launch {
            // Last line of defence: an uncaught exception here kills the whole
            // process, so degrade to an empty table instead.
            val entries = try {
                withContext(Dispatchers.Default) {
                    leaderboardRepository.getLeaderboardWindow(period)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                println("AppViewModel: leaderboard load failed: $error")
                emptyList()
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
