package com.chvma.wordfight.storage

import android.content.Context
import android.content.SharedPreferences
import com.chvma.wordfight.speech.appContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidPreferencesStorage(
    private val context: Context
) : PreferencesStorage {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("wordfight_prefs", Context.MODE_PRIVATE)
    }

    override suspend fun getInt(key: String, default: Int): Int = withContext(Dispatchers.IO) {
        prefs.getInt(key, default)
    }

    override suspend fun putInt(key: String, value: Int) = withContext(Dispatchers.IO) {
        prefs.edit().putInt(key, value).apply()
    }

    override suspend fun getString(key: String, default: String?): String? = withContext(Dispatchers.IO) {
        prefs.getString(key, default)
    }

    override suspend fun putString(key: String, value: String) = withContext(Dispatchers.IO) {
        prefs.edit().putString(key, value).apply()
    }
}

actual fun createPreferencesStorage(): PreferencesStorage {
    return AndroidPreferencesStorage(appContext)
}
