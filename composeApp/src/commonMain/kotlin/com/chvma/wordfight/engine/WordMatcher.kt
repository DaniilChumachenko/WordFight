package com.chvma.wordfight.engine

class WordMatcher {
    fun matches(spoken: String, target: String): Boolean {
        val s = spoken.lowercase().trim()
        val t = target.lowercase().trim()

        if (s == t || s.contains("\\b${Regex.escape(t)}\\b".toRegex())) return true

        // Split spoken text into individual words and fuzzy-match each against target
        return s.split("\\s+".toRegex()).any { spokenWord -> fuzzyMatches(spokenWord, t) }
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
}
