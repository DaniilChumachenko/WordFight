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
        val dayKey = normalizeDailyScoreForUtcDay()
        updateLocalBests(score)
        // Push to the remote leaderboard only once the player has a name.
        // Anonymous games are kept locally and uploaded after registration.
        if (isRegistered()) {
            pushBestsToRemote(dayKey)
        }
    }

    /**
     * Uploads the locally stored best scores to the remote leaderboard.
     * Called right after the player enters their name so that achievements
     * earned before registration immediately appear in the rating.
     */
    suspend fun submitBestScores() {
        if (!isRegistered()) return
        val dayKey = normalizeDailyScoreForUtcDay()
        pushBestsToRemote(dayKey)
    }

    private suspend fun isRegistered(): Boolean {
        return settingsStorage.getPlayerName() != null
    }

    private suspend fun updateLocalBests(score: Int) {
        val currentAllTime = settingsStorage.getBestAllTimeScore()
        if (score > currentAllTime) {
            settingsStorage.setBestAllTimeScore(score)
        }
        val currentDaily = settingsStorage.getBestDailyScore()
        if (score > currentDaily) {
            settingsStorage.setBestDailyScore(score)
        }
    }

    private suspend fun pushBestsToRemote(dayKey: Long) {
        val profile = getOrCreateProfile()
        remoteDataSource.upsertBestScore(
            period = LeaderboardPeriod.ALL_TIME,
            dayKey = null,
            record = profile.toRemote(settingsStorage.getBestAllTimeScore()),
        )
        remoteDataSource.upsertBestScore(
            period = LeaderboardPeriod.TODAY,
            dayKey = dayKey,
            record = profile.toRemote(settingsStorage.getBestDailyScore()),
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

        if (ranked.isEmpty()) return emptyList()

        // Always show the top podium, plus a window of WINDOW_RADIUS entries above
        // and below the current player. Ranks are contiguous within each group, so
        // any gap between them is rendered as an ellipsis by the UI.
        val selfIndex = ranked.indexOfFirst { it.isCurrentPlayer }.coerceAtLeast(0)
        val windowStart = (selfIndex - WINDOW_RADIUS).coerceAtLeast(0)
        val windowEnd = (selfIndex + WINDOW_RADIUS).coerceAtMost(ranked.lastIndex)

        val selected = LinkedHashSet<LeaderboardEntry>()
        ranked.take(TOP_COUNT).forEach(selected::add)
        for (index in windowStart..windowEnd) selected.add(ranked[index])

        return selected.sortedBy { it.rank }
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
        const val TOP_COUNT = 3
        const val WINDOW_RADIUS = 5
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
