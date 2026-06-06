package com.chvma.wordfight

import com.chvma.wordfight.leaderboard.LeaderboardEntry
import com.chvma.wordfight.leaderboard.LeaderboardPeriod
import com.chvma.wordfight.localization.AppLanguage
import com.chvma.wordfight.model.WordContent

/**
 * Single source of truth for the global application state, owned by [AppViewModel].
 * Screens read it as an immutable snapshot and never mutate it directly.
 */
data class AppUiState(
    val isSplashVisible: Boolean = true,
    val language: AppLanguage = AppLanguage.EN,
    val isMenuMusicEnabled: Boolean = true,
    val isGameMusicEnabled: Boolean = true,
    val hasPermission: Boolean = false,
    val isPlayerRegistered: Boolean = false,
    val bestScore: Int = 0,
    val lastScore: Int = 0,
    val lastGameRanked: Boolean = true,
    val lastGameWon: Boolean = false,
    val lastSessionLabel: String = "",
    val missedWords: List<WordContent> = emptyList(),
    val leaderboard: LeaderboardUiState = LeaderboardUiState(),
)

data class LeaderboardUiState(
    val period: LeaderboardPeriod = LeaderboardPeriod.TODAY,
    val entries: List<LeaderboardEntry> = emptyList(),
    val isLoading: Boolean = false,
)
