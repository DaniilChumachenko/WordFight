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
    fun matchesHardCWhenAsrSpellsTheSoundWithK() {
        assertTrue(matcher.matches("Kendi.", "candy"))
        assertTrue(matcher.matches("kamel", "camel"))
    }

    @Test
    fun hardCRuleDoesNotChangeSoftCOrUnrelatedWords() {
        assertFalse(matcher.matches("kitty", "city"))
        assertFalse(matcher.matches("kettle", "candle"))
    }

    @Test
    fun matchesMultiWordTargetsWithSmallAsrDifferences() {
        assertTrue(matcher.matches("live jacket", "life jacket"))
        assertTrue(matcher.matches("flash light", "flashlight"))
    }

    @Test
    fun matchesWordHeardAsSingleLetterName() {
        // ASR spells short words as the letter they sound like.
        assertTrue(matcher.matches("X.", "axe"))
        assertTrue(matcher.matches("ax", "axe"))
    }

    @Test
    fun matchesWordsContainingXAsKsSound() {
        assertTrue(matcher.matches("focks", "fox"))
        assertTrue(matcher.matches("six", "sicks"))
    }

    @Test
    fun matchesFinalConsonantVoicingErrorsFromAsr() {
        assertTrue(matcher.matches("and", "ant"))
        assertTrue(matcher.matches("and and and and", "ant"))
        assertTrue(matcher.matches("bad", "bat"))
        assertTrue(matcher.matches("cab", "cap"))
    }

    @Test
    fun finalConsonantRuleDoesNotAcceptDifferentSoundsOrRoots() {
        assertFalse(matcher.matches("cap", "cat"))
        assertFalse(matcher.matches("art", "ant"))
    }

    @Test
    fun doesNotConfuseShortVowelMinimalPairs() {
        // "ox" and "axe" differ only in the vowel; the single-letter leniency must
        // not leak into ordinary words and make them match each other.
        assertFalse(matcher.matches("ox", "axe"))
        assertFalse(matcher.matches("axe", "ox"))
    }

    @Test
    fun keepsUnrelatedWordsSeparate() {
        assertFalse(matcher.matches("play", "pillow"))
        assertFalse(matcher.matches("spoon", "stairs"))
    }
}
