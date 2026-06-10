package com.chvma.wordfight.leaderboard

import kotlin.test.Test
import kotlin.test.assertEquals

class CountryTest {

    @Test
    fun normalizesIsoCountryCode() {
        assertEquals("UA", normalizeCountryCode(" ua "))
        assertEquals("", normalizeCountryCode("ukr"))
        assertEquals("", normalizeCountryCode(null))
    }

    @Test
    fun buildsFlagEmojiFromCountryCode() {
        assertEquals("\uD83C\uDDFA\uD83C\uDDE6", countryFlagEmoji("UA"))
        assertEquals("\uD83C\uDF10", countryFlagEmoji(""))
    }
}
