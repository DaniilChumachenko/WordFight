package com.chvma.wordfight.storage

import platform.Foundation.NSUserDefaults

class IosPreferencesStorage : PreferencesStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override suspend fun getInt(key: String, default: Int): Int {
        val value = defaults.objectForKey(key) as? platform.Foundation.NSNumber
        return value?.intValue ?: default
    }

    override suspend fun putInt(key: String, value: Int) {
        defaults.setObject(value, forKey = key)
        defaults.synchronize()
    }

    override suspend fun getString(key: String, default: String?): String? {
        return defaults.stringForKey(key) ?: default
    }

    override suspend fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
        defaults.synchronize()
    }
}

actual fun createPreferencesStorage(): PreferencesStorage {
    return IosPreferencesStorage()
}
