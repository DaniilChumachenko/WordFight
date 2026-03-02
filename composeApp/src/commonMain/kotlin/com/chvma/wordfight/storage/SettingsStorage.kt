package com.chvma.wordfight.storage

import com.chvma.wordfight.localization.AppLanguage

class SettingsStorage(
    private val preferences: PreferencesStorage,
) {
    private val languageKey = "app_language"

    suspend fun getLanguage(default: AppLanguage = AppLanguage.EN): AppLanguage {
        val code = preferences.getString(languageKey, null)
        return if (code == null) default else AppLanguage.fromCode(code)
    }

    suspend fun setLanguage(language: AppLanguage) {
        preferences.putString(languageKey, language.code)
    }
}

fun createSettingsStorage(): SettingsStorage {
    return SettingsStorage(createPreferencesStorage())
}
