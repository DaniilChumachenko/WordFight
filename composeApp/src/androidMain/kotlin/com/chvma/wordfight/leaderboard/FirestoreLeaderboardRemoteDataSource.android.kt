package com.chvma.wordfight.leaderboard

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

private const val ALL_TIME_COLLECTION = "leaderboard_all_time"
private const val DAILY_COLLECTION = "leaderboard_daily"
private const val DEFAULT_DAY_KEY = 0L

class FirestoreLeaderboardRemoteDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : LeaderboardRemoteDataSource {

    override suspend fun upsertBestScore(
        period: LeaderboardPeriod,
        dayKey: Long?,
        record: RemoteLeaderboardRecord,
    ) {
        val normalizedDayKey = dayKey ?: DEFAULT_DAY_KEY
        val document = when (period) {
            LeaderboardPeriod.ALL_TIME -> {
                firestore.collection(ALL_TIME_COLLECTION).document(record.playerId)
            }

            LeaderboardPeriod.TODAY -> {
                firestore.collection(DAILY_COLLECTION).document("${normalizedDayKey}_${record.playerId}")
            }
        }

        val snapshot = document.get().await()
        val existingScore = snapshot.getLong("score")?.toInt() ?: 0
        val bestScore = maxOf(existingScore, record.score)

        val payload = hashMapOf<String, Any>(
            "playerId" to record.playerId,
            "name" to record.name,
            "languageCode" to record.languageCode,
            "score" to bestScore,
        )
        if (period == LeaderboardPeriod.TODAY) {
            payload["dayKey"] = normalizedDayKey
        }

        document.set(payload, SetOptions.merge()).await()
    }

    override suspend fun getRecords(
        period: LeaderboardPeriod,
        dayKey: Long?,
    ): List<RemoteLeaderboardRecord> {
        val normalizedDayKey = dayKey ?: DEFAULT_DAY_KEY
        val snapshot = when (period) {
            LeaderboardPeriod.ALL_TIME -> {
                firestore.collection(ALL_TIME_COLLECTION).get().await()
            }

            LeaderboardPeriod.TODAY -> {
                firestore.collection(DAILY_COLLECTION)
                    .whereEqualTo("dayKey", normalizedDayKey)
                    .get()
                    .await()
            }
        }

        return snapshot.documents.mapNotNull { document ->
            val playerId = document.getString("playerId") ?: return@mapNotNull null
            val name = document.getString("name") ?: return@mapNotNull null
            val languageCode = document.getString("languageCode") ?: return@mapNotNull null
            val score = document.getLong("score")?.toInt() ?: 0
            RemoteLeaderboardRecord(
                playerId = playerId,
                name = name,
                languageCode = languageCode,
                score = score,
            )
        }
    }
}

actual fun createLeaderboardRemoteDataSource(): LeaderboardRemoteDataSource {
    return FirestoreLeaderboardRemoteDataSource()
}
