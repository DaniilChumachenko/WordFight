package com.chvma.wordfight.speech

import androidx.compose.runtime.Composable

interface PermissionRequester {
    fun requestPermission(onResult: (Boolean) -> Unit)
}

@Composable
expect fun rememberPermissionRequester(
    onPermissionResult: (Boolean) -> Unit
): PermissionRequester
