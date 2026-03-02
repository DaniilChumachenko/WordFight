package com.chvma.wordfight.storage

import com.chvma.wordfight.content.WordRepository
import com.chvma.wordfight.model.WordContent

interface PreferencesStorage {
    suspend fun getInt(key: String, default: Int): Int
    suspend fun putInt(key: String, value: Int)
    suspend fun getString(key: String, default: String?): String?
    suspend fun putString(key: String, value: String)
}

expect fun createPreferencesStorage(): PreferencesStorage

class WordStorageImpl(
    private val preferences: PreferencesStorage
) : WordStorage {
    private val wordsKey = "saved_words"
    private val bestScoreKey = "best_score"
    private val separator = "|||"
    private val itemSeparator = ":::"

    override suspend fun saveWord(word: WordContent) {
        val words = getAllWords().toMutableList()
        if (words.none { it.id == word.id }) {
            words.add(word)
            saveWords(words)
        }
    }

    override suspend fun getAllWords(): List<WordContent> {
        val data = preferences.getString(wordsKey, null) ?: return emptyList()
        if (data.isEmpty()) return emptyList()

        return try {
            data.split(separator).mapNotNull { item ->
                val parts = item.split(itemSeparator)
                val id = parts.firstOrNull()?.toIntOrNull() ?: return@mapNotNull null
                WordRepository.byId(id)
                    ?: parts.getOrNull(1)?.let { WordRepository.byWord(it) }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun deleteWord(word: String) {
        val words = getAllWords().filter { it.word != word }
        saveWords(words)
    }

    override suspend fun isWordSaved(word: String): Boolean {
        return getAllWords().any { it.word == word }
    }

    private suspend fun saveWords(words: List<WordContent>) {
        val data = words.joinToString(separator) { word ->
            "${word.id}"
        }
        preferences.putString(wordsKey, data)
    }

    override suspend fun getBestScore(): Int {
        return preferences.getInt(bestScoreKey, 0)
    }

    override suspend fun saveBestScore(score: Int) {
        preferences.putInt(bestScoreKey, score)
    }
}
