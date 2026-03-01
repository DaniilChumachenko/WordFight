package com.chvma.wordfight.speech

interface PermissionManager {
    suspend fun hasPermission(): Boolean
    suspend fun requestPermission(): Boolean
}

expect fun createPermissionManager(): PermissionManager
