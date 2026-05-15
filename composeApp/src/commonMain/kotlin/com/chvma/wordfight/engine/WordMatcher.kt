package com.chvma.wordfight.engine

import kotlin.math.abs

class WordMatcher {
    fun matches(spoken: String, target: String): Boolean {
        val s = normalizePhrase(spoken)
        val t = normalizePhrase(target)

        if (s.isBlank() || t.isBlank()) return false

        if (containsWholePhrase(s, t) || compact(s) == compact(t)) return true

        val targetAliases = aliasesFor(t)
        if (targetAliases.any { alias ->
                containsWholePhrase(s, alias) ||
                    compact(s) == compact(alias) ||
                    phraseFuzzyMatches(s, alias)
            }
        ) {
            return true
        }

        return phraseFuzzyMatches(s, t)
    }

    private fun phraseFuzzyMatches(spoken: String, target: String): Boolean {
        val spokenTokens = words(spoken)
        val targetTokens = words(target)
        if (spokenTokens.isEmpty() || targetTokens.isEmpty()) return false

        if (targetTokens.size == 1) {
            val targetToken = targetTokens.first()
            return spokenTokens.any { spokenToken ->
                tokenVariants(spokenToken).any { spokenVariant ->
                    tokenMatches(spokenVariant, targetToken)
                }
            }
        }

        // Multi-word cards like "life jacket" or "washing machine" should survive
        // small ASR differences in each word, without requiring an exact phrase.
        return targetTokens.all { targetToken ->
            spokenTokens.any { spokenToken ->
                tokenVariants(spokenToken).any { spokenVariant ->
                    tokenMatches(spokenVariant, targetToken)
                }
            }
        }
    }

    private fun tokenMatches(spoken: String, target: String): Boolean {
        if (spoken == target) return true
        if (fuzzyMatches(spoken, target)) return true
        return phoneticMatches(spoken, target)
    }

    private fun fuzzyMatches(spoken: String, target: String): Boolean {
        // Allow edit distance proportional to word length:
        // ≤3 chars → exact only, 4-5 chars → 1 difference, 6+ chars → 2 differences
        val maxDistance = when {
            target.length <= 3 -> 0
            target.length <= 5 -> 1
            else -> 2
        }
        return levenshtein(spoken, target) <= maxDistance
    }

    private fun phoneticMatches(spoken: String, target: String): Boolean {
        if (spoken.length < 2 || target.length < 3) return false
        if (firstSound(spoken) != firstSound(target)) return false

        val spokenKey = phoneticKey(spoken)
        val targetKey = phoneticKey(target)
        if (spokenKey.length < 2 || targetKey.length < 2) return false

        val maxDistance = if (target.length <= 3) 0 else 1
        if (levenshtein(spokenKey, targetKey) > maxDistance) return false

        return abs(spoken.length - target.length) <= 2 || spokenKey == targetKey
    }

    private fun aliasesFor(target: String): Set<String> {
        val aliases = mutableSetOf<String>()
        asrAliases[target]?.let(aliases::addAll)
        words(target).singleOrNull()?.let { token ->
            if (token.endsWith("ate") && token.length > 4) {
                aliases += token.removeSuffix("ate") + "ay"
            }
        }
        return aliases
    }

    private fun tokenVariants(token: String): Set<String> {
        val variants = mutableSetOf(token)
        fun addIfUseful(value: String) {
            if (value.length >= 2) variants += value
        }

        if (token.endsWith("'s")) addIfUseful(token.dropLast(2))
        if (token.endsWith("s") && token.length > 3) addIfUseful(token.dropLast(1))
        if (token.endsWith("es") && token.length > 4) addIfUseful(token.dropLast(2))
        if (token.endsWith("ed") && token.length > 4) addIfUseful(token.dropLast(2))
        if (token.endsWith("ing") && token.length > 5) addIfUseful(token.dropLast(3))
        return variants
    }

    private fun phoneticKey(value: String): String {
        var word = value
            .replace("'", "")
            .replace(Regex("^kn"), "n")
            .replace(Regex("^wr"), "r")
            .replace(Regex("^wh"), "w")
            .replace("ph", "f")
            .replace("ck", "k")
            .replace("ght", "t")
            .replace("gh", "")

        if (word.length > 3 && word.endsWith("e")) {
            word = word.dropLast(1)
        }

        return word
            .replace(Regex("[aeiouy]+"), "A")
            .replace(Regex("(.)\\1+"), "$1")
    }

    private fun firstSound(value: String): Char {
        val normalized = when {
            value.startsWith("kn") -> value.drop(1)
            value.startsWith("wr") -> value.drop(1)
            value.startsWith("wh") -> "w" + value.drop(2)
            value.startsWith("ph") -> "f" + value.drop(2)
            else -> value
        }
        return normalized.first()
    }

    private fun containsWholePhrase(spoken: String, target: String): Boolean {
        if (spoken == target) return true
        return Regex("(^|\\s)${Regex.escape(target)}($|\\s)").containsMatchIn(spoken)
    }

    private fun normalizePhrase(value: String): String =
        value.lowercase()
            .replace(Regex("[^a-z'\\s-]"), " ")
            .replace("-", " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun words(value: String): List<String> =
        normalizePhrase(value).split(" ").filter { it.isNotBlank() }

    private fun compact(value: String): String = words(value).joinToString("")

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                dp[i][j] = if (a[i - 1] == b[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
                }
            }
        }
        return dp[a.length][b.length]
    }

    private companion object {
        val asrAliases = mapOf(
            "plate" to setOf("play", "played", "plait"),
            "wheel" to setOf("will", "we'll", "well"),
            "spoon" to setOf("spun"),
            "thread" to setOf("red"),
            "match" to setOf("much"),
            "ruler" to setOf("roller"),
            "flashlight" to setOf("flash light", "flesh light"),
        )
    }
}
