package com.chvma.wordfight.leaderboard

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

private const val ALL_TIME_COLLECTION = "leaderboard_all_time"
private const val DAILY_COLLECTION = "leaderboard_daily"
private const val DEFAULT_DAY_KEY = 0L

// Temporary diagnostics for the composite index on leaderboard_daily.
private const val INDEX_TAG = "INDEXWORK"

// The UI shows the podium plus a small window around the player, so a top
// slice is enough. Fetching whole collections does not scale: read cost and
// latency grow with every registered player.
private const val MAX_RECORDS = 100L

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

        // Transaction keeps a concurrent submission from overwriting a higher
        // score with a stale read.
        firestore.runTransaction { transaction ->
            val existingScore = transaction.get(document).getLong("score")?.toInt() ?: 0
            val bestScore = maxOf(existingScore, record.score)

            val payload = hashMapOf<String, Any>(
                "playerId" to record.playerId,
                "name" to record.name,
                "countryCode" to record.countryCode,
                "score" to bestScore,
            )
            if (period == LeaderboardPeriod.TODAY) {
                payload["dayKey"] = normalizedDayKey
            }
            transaction.set(document, payload)
        }.await()
    }

    override suspend fun getRecords(
        period: LeaderboardPeriod,
        dayKey: Long?,
    ): List<RemoteLeaderboardRecord> {
        val normalizedDayKey = dayKey ?: DEFAULT_DAY_KEY
        val query = when (period) {
            LeaderboardPeriod.ALL_TIME -> firestore.collection(ALL_TIME_COLLECTION)

            // Needs a composite index on leaderboard_daily (dayKey ASC,
            // score DESC); Firestore logs a one-click creation link on first use.
            LeaderboardPeriod.TODAY -> firestore.collection(DAILY_COLLECTION)
                .whereEqualTo("dayKey", normalizedDayKey)
        }
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(MAX_RECORDS)

        Log.d(INDEX_TAG, "getRecords: period=$period dayKey=$normalizedDayKey limit=$MAX_RECORDS")
        val snapshot = try {
            query.get().await()
        } catch (error: Exception) {
            Log.e(INDEX_TAG, "getRecords FAILED: period=$period dayKey=$normalizedDayKey", error)
            throw error
        }

        val records = snapshot.documents.mapNotNull { document ->
            val playerId = document.getString("playerId") ?: return@mapNotNull null
            val name = document.getString("name") ?: return@mapNotNull null
            val countryCode = document.getString("countryCode").orEmpty()
            val score = document.getLong("score")?.toInt() ?: 0
            RemoteLeaderboardRecord(
                playerId = playerId,
                name = name,
                languageCode = "",
                countryCode = countryCode,
                score = score,
            )
        }
        Log.d(
            INDEX_TAG,
            "getRecords OK: period=$period docs=${snapshot.size()} -> " +
                records.joinToString(prefix = "[", postfix = "]") { "${it.name}=${it.score}" },
        )
        return records
    }
}

actual fun createLeaderboardRemoteDataSource(): LeaderboardRemoteDataSource {
    return FirestoreLeaderboardRemoteDataSource()
}
