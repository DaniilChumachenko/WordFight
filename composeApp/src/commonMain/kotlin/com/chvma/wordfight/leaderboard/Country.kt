package com.chvma.wordfight.leaderboard

private const val REGIONAL_INDICATOR_A = 0x1F1E6
private const val ASCII_A = 0x41
private const val HIGH_SURROGATE_START = 0xD800
private const val LOW_SURROGATE_START = 0xDC00
private const val SUPPLEMENTARY_CODE_POINT_OFFSET = 0x10000

expect fun currentCountryCode(): String?

expect fun usesCountryInLeaderboard(): Boolean

fun normalizeCountryCode(countryCode: String?): String {
    val normalized = countryCode?.trim()?.uppercase().orEmpty()
    return normalized.takeIf { code ->
        code.length == 2 && code.all { it in 'A'..'Z' }
    }.orEmpty()
}

fun countryFlagEmoji(countryCode: String?): String {
    val normalized = normalizeCountryCode(countryCode)
    if (normalized.isEmpty()) return "\uD83C\uDF10"

    return buildString {
        normalized.forEach { letter ->
            appendCodePointCompat(REGIONAL_INDICATOR_A + (letter.code - ASCII_A))
        }
    }
}

private fun StringBuilder.appendCodePointCompat(codePoint: Int) {
    val supplementary = codePoint - SUPPLEMENTARY_CODE_POINT_OFFSET
    append((HIGH_SURROGATE_START + (supplementary shr 10)).toChar())
    append((LOW_SURROGATE_START + (supplementary and 0x3FF)).toChar())
}
