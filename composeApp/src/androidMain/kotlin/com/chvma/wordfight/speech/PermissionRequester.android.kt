package com.chvma.wordfight.speech

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
actual fun rememberPermissionRequester(
    onPermissionResult: (Boolean) -> Unit
): PermissionRequester {
    val currentCallback = remember { mutableStateOf<((Boolean) -> Unit)?>(null) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        currentCallback.value?.invoke(isGranted)
        currentCallback.value = null
        onPermissionResult(isGranted)
    }

    return object : PermissionRequester {
        override fun requestPermission(onResult: (Boolean) -> Unit) {
            currentCallback.value = onResult
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}
