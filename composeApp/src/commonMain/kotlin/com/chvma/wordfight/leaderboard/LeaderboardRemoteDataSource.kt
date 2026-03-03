package com.chvma.wordfight.leaderboard

interface LeaderboardRemoteDataSource {
    suspend fun upsertBestScore(
        period: LeaderboardPeriod,
        dayKey: Long?,
        record: RemoteLeaderboardRecord,
    )

    suspend fun getRecords(
        period: LeaderboardPeriod,
        dayKey: Long?,
    ): List<RemoteLeaderboardRecord>
}

expect fun createLeaderboardRemoteDataSource(): LeaderboardRemoteDataSource
