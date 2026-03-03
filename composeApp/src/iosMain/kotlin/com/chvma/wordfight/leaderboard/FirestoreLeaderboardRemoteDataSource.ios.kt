package com.chvma.wordfight.leaderboard

import cocoapods.FirebaseFirestoreInternal.FIRCollectionReference
import cocoapods.FirebaseFirestoreInternal.FIRDocumentReference
import cocoapods.FirebaseFirestoreInternal.FIRDocumentSnapshot
import cocoapods.FirebaseFirestoreInternal.FIRFirestore
import cocoapods.FirebaseFirestoreInternal.FIRQuerySnapshot
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private const val ALL_TIME_COLLECTION = "leaderboard_all_time"
private const val DAILY_COLLECTION = "leaderboard_daily"
private const val DEFAULT_DAY_KEY = 0L

@OptIn(ExperimentalForeignApi::class)
class FirestoreLeaderboardRemoteDataSource(
    private val firestore: FIRFirestore = configuredFirestore(),
) : LeaderboardRemoteDataSource {

    override suspend fun upsertBestScore(
        period: LeaderboardPeriod,
        dayKey: Long?,
        record: RemoteLeaderboardRecord,
    ) {
        val normalizedDayKey = dayKey ?: DEFAULT_DAY_KEY
        val document = when (period) {
            LeaderboardPeriod.ALL_TIME -> {
                firestore.collectionWithPath(ALL_TIME_COLLECTION).documentWithPath(record.playerId)
            }

            LeaderboardPeriod.TODAY -> {
                firestore.collectionWithPath(DAILY_COLLECTION)
                    .documentWithPath("${normalizedDayKey}_${record.playerId}")
            }
        }

        val existing = awaitDocument(document)
        val existingScore = (existing?.data()?.get("score") as? Number)?.toInt() ?: 0
        val bestScore = maxOf(existingScore, record.score)

        val payload = mutableMapOf<Any?, Any?>(
            "playerId" to record.playerId,
            "name" to record.name,
            "languageCode" to record.languageCode,
            "score" to bestScore,
        )
        if (period == LeaderboardPeriod.TODAY) {
            payload["dayKey"] = normalizedDayKey
        }

        document.setData(payload)
    }

    override suspend fun getRecords(
        period: LeaderboardPeriod,
        dayKey: Long?,
    ): List<RemoteLeaderboardRecord> {
        val normalizedDayKey = dayKey ?: DEFAULT_DAY_KEY
        val snapshot = when (period) {
            LeaderboardPeriod.ALL_TIME -> {
                awaitDocuments(firestore.collectionWithPath(ALL_TIME_COLLECTION))
            }

            LeaderboardPeriod.TODAY -> {
                awaitDocuments(firestore.collectionWithPath(DAILY_COLLECTION))
            }
        } ?: return emptyList()

        val documents = snapshot.documents as? List<*> ?: return emptyList()

        return documents.mapNotNull { rawDocument ->
            val doc = rawDocument as? FIRDocumentSnapshot ?: return@mapNotNull null
            val data = doc.data() as? Map<*, *> ?: return@mapNotNull null
            if (period == LeaderboardPeriod.TODAY) {
                val day = (data["dayKey"] as? Number)?.toLong() ?: return@mapNotNull null
                if (day != normalizedDayKey) return@mapNotNull null
            }
            val playerId = (data["playerId"] as? String) ?: return@mapNotNull null
            val name = (data["name"] as? String) ?: return@mapNotNull null
            val languageCode = (data["languageCode"] as? String) ?: return@mapNotNull null
            val score = (data["score"] as? Number)?.toInt() ?: 0

            RemoteLeaderboardRecord(
                playerId = playerId,
                name = name,
                languageCode = languageCode,
                score = score,
            )
        }
    }

    private suspend fun awaitDocument(document: FIRDocumentReference): FIRDocumentSnapshot? {
        return suspendCancellableCoroutine { continuation ->
            document.getDocumentWithCompletion { snapshot, _ ->
                continuation.resume(snapshot)
            }
        }
    }

    private suspend fun awaitDocuments(collection: FIRCollectionReference): FIRQuerySnapshot? {
        return suspendCancellableCoroutine { continuation ->
            collection.getDocumentsWithCompletion { snapshot, _ ->
                continuation.resume(snapshot)
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun configuredFirestore(): FIRFirestore {
    return FIRFirestore.firestore()
}

@OptIn(ExperimentalForeignApi::class)
actual fun createLeaderboardRemoteDataSource(): LeaderboardRemoteDataSource {
    return FirestoreLeaderboardRemoteDataSource()
}
