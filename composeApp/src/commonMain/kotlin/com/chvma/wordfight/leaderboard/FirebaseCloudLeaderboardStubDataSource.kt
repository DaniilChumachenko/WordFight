package com.chvma.wordfight.leaderboard

import com.chvma.wordfight.localization.AppLanguage
import kotlin.random.Random

class FirebaseCloudLeaderboardStubDataSource : LeaderboardRemoteDataSource {
    override suspend fun upsertBestScore(
        period: LeaderboardPeriod,
        dayKey: Long?,
        record: RemoteLeaderboardRecord,
    ) {
        val target = when (period) {
            LeaderboardPeriod.TODAY -> dailyRecords.getOrPut(dayKey ?: 0L) { mutableMapOf() }
            LeaderboardPeriod.ALL_TIME -> allTimeRecords
        }
        val existing = target[record.playerId]
        val bestScore = maxOf(existing?.score ?: 0, record.score)
        target[record.playerId] = record.copy(score = bestScore)
    }

    override suspend fun getRecords(
        period: LeaderboardPeriod,
        dayKey: Long?,
    ): List<RemoteLeaderboardRecord> {
        val dynamic = when (period) {
            LeaderboardPeriod.TODAY -> dailyRecords[dayKey ?: 0L].orEmpty().values
            LeaderboardPeriod.ALL_TIME -> allTimeRecords.values
        }
        return generatedBots(period, dayKey) + dynamic
    }

    private fun generatedBots(
        period: LeaderboardPeriod,
        dayKey: Long?,
    ): List<RemoteLeaderboardRecord> {
        val seed = when (period) {
            LeaderboardPeriod.TODAY -> ((dayKey ?: 0L) xor 0x5BD1E995L).toInt()
            LeaderboardPeriod.ALL_TIME -> 0x6E624EB7
        }
        val random = Random(seed)
        return botNames.mapIndexed { index, name ->
            val language = AppLanguage.supported[random.nextInt(AppLanguage.supported.size)]
            val score = when (period) {
                LeaderboardPeriod.TODAY -> 60 - index + random.nextInt(0, 6)
                LeaderboardPeriod.ALL_TIME -> 320 - index * 3 + random.nextInt(0, 9)
            }.coerceAtLeast(1)
            RemoteLeaderboardRecord(
                playerId = "bot_${period.name.lowercase()}_$index",
                name = name,
                languageCode = language.code,
                score = score,
            )
        }
    }

    private companion object {
        val allTimeRecords = mutableMapOf<String, RemoteLeaderboardRecord>()
        val dailyRecords = mutableMapOf<Long, MutableMap<String, RemoteLeaderboardRecord>>()

        val botNames = listOf(
            "Alex", "Mila", "Ilya", "Olivia", "Noah", "Dany", "Emma", "Liam", "Lucas", "Sofia",
            "Ethan", "Ava", "Mason", "Amelia", "James", "Mia", "Logan", "Ella", "Henry", "Luna",
            "Jack", "Aria", "Leo", "Nora", "Mark", "Iris", "Owen", "Eva", "Ryan", "Zoe",
        )
    }
}
