package com.chvma.wordfight.leaderboard

import com.chvma.wordfight.localization.AppLanguage
import com.chvma.wordfight.storage.SettingsStorage

class LeaderboardRepository(
    private val settingsStorage: SettingsStorage,
    private val remoteDataSource: LeaderboardRemoteDataSource,
) {
    suspend fun syncAllTimeBaseline(score: Int) {
        if (score <= 0) return
        val current = settingsStorage.getBestAllTimeScore()
        if (score > current) {
            settingsStorage.setBestAllTimeScore(score)
        }
    }

    suspend fun submitGameScore(score: Int) {
        if (score <= 0) return
        val profile = getOrCreateProfile()
        val dayKey = normalizeDailyScoreForUtcDay()

        val currentAllTime = settingsStorage.getBestAllTimeScore()
        val allTimeBest = maxOf(score, currentAllTime)
        if (allTimeBest > currentAllTime) {
            settingsStorage.setBestAllTimeScore(allTimeBest)
        }
        remoteDataSource.upsertBestScore(
            period = LeaderboardPeriod.ALL_TIME,
            dayKey = null,
            record = profile.toRemote(allTimeBest),
        )

        val currentDaily = settingsStorage.getBestDailyScore()
        val dailyBest = maxOf(score, currentDaily)
        if (dailyBest > currentDaily) {
            settingsStorage.setBestDailyScore(dailyBest)
        }
        remoteDataSource.upsertBestScore(
            period = LeaderboardPeriod.TODAY,
            dayKey = dayKey,
            record = profile.toRemote(dailyBest),
        )
    }

    suspend fun getLeaderboardWindow(period: LeaderboardPeriod): List<LeaderboardEntry> {
        val profile = getOrCreateProfile()
        val dayKey = normalizeDailyScoreForUtcDay()
        val localScore = when (period) {
            LeaderboardPeriod.TODAY -> settingsStorage.getBestDailyScore()
            LeaderboardPeriod.ALL_TIME -> settingsStorage.getBestAllTimeScore()
        }

        remoteDataSource.upsertBestScore(
            period = period,
            dayKey = if (period == LeaderboardPeriod.TODAY) dayKey else null,
            record = profile.toRemote(localScore),
        )

        val records = remoteDataSource.getRecords(
            period = period,
            dayKey = if (period == LeaderboardPeriod.TODAY) dayKey else null,
        )

        val merged = records
            .plus(profile.toRemote(localScore))
            .groupBy { it.playerId }
            .values
            .map { grouped -> grouped.maxByOrNull { it.score } ?: grouped.first() }
            .sortedWith(
                compareByDescending<RemoteLeaderboardRecord> { it.score }
                    .thenBy { it.name.lowercase() },
            )

        val ranked = merged.mapIndexed { index, record ->
            LeaderboardEntry(
                rank = index + 1,
                playerId = record.playerId,
                name = record.name,
                language = AppLanguage.fromCode(record.languageCode),
                score = record.score,
                isCurrentPlayer = record.playerId == profile.playerId,
            )
        }

        val selfIndex = ranked.indexOfFirst { it.isCurrentPlayer }.let { if (it < 0) 0 else it }
        val start = (selfIndex - WINDOW_RADIUS).coerceAtLeast(0)
        val end = (selfIndex + WINDOW_RADIUS).coerceAtMost(ranked.lastIndex)

        return if (ranked.isEmpty()) emptyList() else ranked.subList(start, end + 1)
    }

    private suspend fun getOrCreateProfile(): PlayerProfile {
        val playerId = settingsStorage.ensurePlayerId()
        val name = settingsStorage.getPlayerName().orEmpty().ifBlank { DEFAULT_PLAYER_NAME }
        val language = settingsStorage.getLanguage()
        return PlayerProfile(
            playerId = playerId,
            name = name,
            language = language,
        )
    }

    private suspend fun normalizeDailyScoreForUtcDay(): Long {
        val utcDayKey = currentTimeMillis() / MILLIS_IN_DAY
        val storedDayKey = settingsStorage.getBestDailyDayKey()
        if (storedDayKey != utcDayKey) {
            settingsStorage.setBestDailyDayKey(utcDayKey)
            settingsStorage.setBestDailyScore(0)
        }
        return utcDayKey
    }

    private data class PlayerProfile(
        val playerId: String,
        val name: String,
        val language: AppLanguage,
    ) {
        fun toRemote(score: Int): RemoteLeaderboardRecord {
            return RemoteLeaderboardRecord(
                playerId = playerId,
                name = name,
                languageCode = language.code,
                score = score.coerceAtLeast(0),
            )
        }
    }

    private companion object {
        const val WINDOW_RADIUS = 4
        const val MILLIS_IN_DAY = 86_400_000L
        const val DEFAULT_PLAYER_NAME = "Player"
    }
}

fun createLeaderboardRepository(settingsStorage: SettingsStorage): LeaderboardRepository {
    return LeaderboardRepository(
        settingsStorage = settingsStorage,
        remoteDataSource = createLeaderboardRemoteDataSource(),
    )
}
