package com.chvma.wordfight.speech

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.chvma.wordfight.speech.appContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidPermissionManager : PermissionManager {
    override suspend fun hasPermission(): Boolean = withContext(Dispatchers.IO) {
        ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun requestPermission(): Boolean {
        // Permission request should be handled in Activity
        // This is a placeholder - actual implementation would need Activity reference
        return hasPermission()
    }
}

actual fun createPermissionManager(): PermissionManager {
    return AndroidPermissionManager()
}
