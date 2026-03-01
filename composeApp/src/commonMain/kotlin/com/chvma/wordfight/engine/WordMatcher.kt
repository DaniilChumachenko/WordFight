package com.chvma.wordfight.engine

class WordMatcher {
    fun matches(spoken: String, target: String): Boolean {
        val s = spoken.lowercase().trim()
        val t = target.lowercase().trim()
        return s == t || s.contains("\\b$t\\b".toRegex())
    }
}
