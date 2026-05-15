package com.chvma.wordfight.engine

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WordMatcherTest {
    private val matcher = WordMatcher()

    @Test
    fun matchesPlateWhenAsrDropsFinalSound() {
        assertTrue(matcher.matches("play", "plate"))
        assertTrue(matcher.matches("played", "plate"))
    }

    @Test
    fun matchesShortWordsWithVowelConfusion() {
        assertTrue(matcher.matches("pen", "pan"))
        assertTrue(matcher.matches("soup", "soap"))
    }

    @Test
    fun matchesMultiWordTargetsWithSmallAsrDifferences() {
        assertTrue(matcher.matches("live jacket", "life jacket"))
        assertTrue(matcher.matches("flash light", "flashlight"))
    }

    @Test
    fun keepsUnrelatedWordsSeparate() {
        assertFalse(matcher.matches("play", "pillow"))
        assertFalse(matcher.matches("spoon", "stairs"))
    }
}
