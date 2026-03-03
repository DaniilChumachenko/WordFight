package com.chvma.wordfight.leaderboard

import com.chvma.wordfight.localization.AppLanguage

enum class LeaderboardPeriod {
    TODAY,
    ALL_TIME,
}

data class LeaderboardEntry(
    val rank: Int,
    val playerId: String,
    val name: String,
    val language: AppLanguage,
    val score: Int,
    val isCurrentPlayer: Boolean,
)

data class RemoteLeaderboardRecord(
    val playerId: String,
    val name: String,
    val languageCode: String,
    val score: Int,
)
