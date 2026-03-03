package com.chvma.wordfight.storage

import com.chvma.wordfight.localization.AppLanguage
import kotlin.random.Random

class SettingsStorage(
    private val preferences: PreferencesStorage,
) {
    private val languageKey = "app_language"
    private val playerNameKey = "player_name"
    private val playerIdKey = "player_id"
    private val onboardingCompletedKey = "onboarding_completed"
    private val bestDailyScoreKey = "leaderboard_best_daily"
    private val bestAllTimeScoreKey = "leaderboard_best_all_time"
    private val bestDailyDayKey = "leaderboard_daily_day_key"

    suspend fun getLanguage(default: AppLanguage = AppLanguage.EN): AppLanguage {
        val code = preferences.getString(languageKey, null)
        return if (code == null) default else AppLanguage.fromCode(code)
    }

    suspend fun setLanguage(language: AppLanguage) {
        preferences.putString(languageKey, language.code)
    }

    suspend fun getPlayerName(): String? {
        return preferences.getString(playerNameKey, null)?.trim()?.takeIf { it.isNotEmpty() }
    }

    suspend fun setPlayerName(name: String) {
        preferences.putString(playerNameKey, name.trim())
    }

    suspend fun getPlayerId(): String? {
        return preferences.getString(playerIdKey, null)?.takeIf { it.isNotBlank() }
    }

    suspend fun ensurePlayerId(): String {
        val existing = getPlayerId()
        if (existing != null) return existing
        val generated = generatePlayerId()
        preferences.putString(playerIdKey, generated)
        return generated
    }

    suspend fun isOnboardingCompleted(): Boolean {
        return preferences.getInt(onboardingCompletedKey, 0) == 1
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        preferences.putInt(onboardingCompletedKey, if (completed) 1 else 0)
    }

    suspend fun getBestDailyScore(): Int {
        return preferences.getInt(bestDailyScoreKey, 0)
    }

    suspend fun setBestDailyScore(score: Int) {
        preferences.putInt(bestDailyScoreKey, score)
    }

    suspend fun getBestAllTimeScore(): Int {
        return preferences.getInt(bestAllTimeScoreKey, 0)
    }

    suspend fun setBestAllTimeScore(score: Int) {
        preferences.putInt(bestAllTimeScoreKey, score)
    }

    suspend fun getBestDailyDayKey(): Long {
        return preferences.getString(bestDailyDayKey, "0")?.toLongOrNull() ?: 0L
    }

    suspend fun setBestDailyDayKey(dayKey: Long) {
        preferences.putString(bestDailyDayKey, dayKey.toString())
    }

    private fun generatePlayerId(): String {
        val p1 = Random.nextLong().toULong().toString(16)
        val p2 = Random.nextLong().toULong().toString(16)
        return "wf_${p1}${p2}"
    }
}

fun createSettingsStorage(): SettingsStorage {
    return SettingsStorage(createPreferencesStorage())
}
