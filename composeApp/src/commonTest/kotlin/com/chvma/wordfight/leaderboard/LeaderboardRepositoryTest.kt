package com.chvma.wordfight.leaderboard

import com.chvma.wordfight.storage.PreferencesStorage
import com.chvma.wordfight.storage.SettingsStorage
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaderboardRepositoryTest {

    @Test
    fun submitsSystemCountryInsteadOfLearningLanguage() = runBlocking {
        val settings = SettingsStorage(InMemoryPreferencesStorage()).apply {
            setPlayerName("Player")
            setBestAllTimeScore(42)
        }
        val remote = RecordingLeaderboardRemoteDataSource()
        val repository = LeaderboardRepository(
            settingsStorage = settings,
            remoteDataSource = remote,
            countryCodeProvider = { " ua " },
        )

        repository.submitBestScores()

        assertTrue(remote.submittedRecords.isNotEmpty())
        assertTrue(remote.submittedRecords.all { it.countryCode == "UA" })
        assertEquals(42, remote.submittedRecords.first { it.period == LeaderboardPeriod.ALL_TIME }.score)
    }
}

private class RecordingLeaderboardRemoteDataSource : LeaderboardRemoteDataSource {
    val submittedRecords = mutableListOf<SubmittedRecord>()

    override suspend fun upsertBestScore(
        period: LeaderboardPeriod,
        dayKey: Long?,
        record: RemoteLeaderboardRecord,
    ) {
        submittedRecords += SubmittedRecord(period, record.languageCode, record.countryCode, record.score)
    }

    override suspend fun getRecords(
        period: LeaderboardPeriod,
        dayKey: Long?,
    ): List<RemoteLeaderboardRecord> = emptyList()
}

private data class SubmittedRecord(
    val period: LeaderboardPeriod,
    val languageCode: String,
    val countryCode: String,
    val score: Int,
)

private class InMemoryPreferencesStorage : PreferencesStorage {
    private val ints = mutableMapOf<String, Int>()
    private val strings = mutableMapOf<String, String>()

    override suspend fun getInt(key: String, default: Int): Int = ints[key] ?: default

    override suspend fun putInt(key: String, value: Int) {
        ints[key] = value
    }

    override suspend fun getString(key: String, default: String?): String? = strings[key] ?: default

    override suspend fun putString(key: String, value: String) {
        strings[key] = value
    }
}
