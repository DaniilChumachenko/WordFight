package com.chvma.wordfight.leaderboard

import com.chvma.wordfight.localization.AppLanguage
import com.chvma.wordfight.storage.SettingsStorage
import kotlin.coroutines.cancellation.CancellationException

class LeaderboardRepository(
    private val settingsStorage: SettingsStorage,
    private val remoteDataSource: LeaderboardRemoteDataSource,
    private val countryCodeProvider: () -> String? = ::currentCountryCode,
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
        val bestAllTime = settingsStorage.getBestAllTimeScore()
        val bestDaily = settingsStorage.getBestDailyScore()
        // A network failure must never crash the caller (game over happens
        // offline too, and Firestore transactions require connectivity). The
        // local bests stay the source of truth and are re-pushed by the
        // self-heal on the next leaderboard read.
        try {
            // Zero scores never reach the server: a player who registered but
            // has not finished a game yet would otherwise litter the rating
            // with empty records.
            if (bestAllTime > 0) {
                remoteDataSource.upsertBestScore(
                    period = LeaderboardPeriod.ALL_TIME,
                    dayKey = null,
                    record = profile.toRemote(bestAllTime),
                )
            }
            if (bestDaily > 0) {
                remoteDataSource.upsertBestScore(
                    period = LeaderboardPeriod.TODAY,
                    dayKey = dayKey,
                    record = profile.toRemote(bestDaily),
                )
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            println("LeaderboardRepository: failed to push best scores: $error")
        }
    }

    suspend fun getLeaderboardWindow(period: LeaderboardPeriod): List<LeaderboardEntry> {
        val profile = getOrCreateProfile()
        val dayKey = normalizeDailyScoreForUtcDay()
        val localScore = when (period) {
            LeaderboardPeriod.TODAY -> settingsStorage.getBestDailyScore()
            LeaderboardPeriod.ALL_TIME -> settingsStorage.getBestAllTimeScore()
        }

        // null = fetch failed (offline, missing index, backend error). The
        // screen then degrades to the local best instead of crashing the app.
        val records: List<RemoteLeaderboardRecord>? = try {
            remoteDataSource.getRecords(
                period = period,
                dayKey = if (period == LeaderboardPeriod.TODAY) dayKey else null,
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            println("LeaderboardRepository: failed to fetch leaderboard: $error")
            null
        }

        // Self-heal: push the local best only when the server clearly lags
        // behind (e.g. a submission after a game failed). Writing on every
        // read would double the round-trips for no benefit. Skipped when the
        // fetch failed — the connection is likely down anyway.
        if (records != null) {
            val remoteSelfScore = records.firstOrNull { it.playerId == profile.playerId }?.score ?: 0
            if (localScore > remoteSelfScore) {
                try {
                    remoteDataSource.upsertBestScore(
                        period = period,
                        dayKey = if (period == LeaderboardPeriod.TODAY) dayKey else null,
                        record = profile.toRemote(localScore),
                    )
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (error: Exception) {
                    println("LeaderboardRepository: failed to self-heal score: $error")
                }
            }
        }

        val merged = records.orEmpty()
            // Hide legacy zero-score records already in the database; the
            // current player is re-added from the local best below, so they
            // always see themselves even with 0 points.
            .filter { it.score > 0 }
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
                countryCode = normalizeCountryCode(record.countryCode),
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
            countryCode = normalizeCountryCode(countryCodeProvider()),
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
        val countryCode: String,
    ) {
        fun toRemote(score: Int): RemoteLeaderboardRecord {
            return RemoteLeaderboardRecord(
                playerId = playerId,
                name = name,
                languageCode = language.code,
                countryCode = countryCode,
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
