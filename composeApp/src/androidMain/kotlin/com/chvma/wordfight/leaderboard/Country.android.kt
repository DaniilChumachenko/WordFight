package com.chvma.wordfight.leaderboard

import java.util.Locale

actual fun currentCountryCode(): String? = Locale.getDefault().country

actual fun usesCountryInLeaderboard(): Boolean = true
