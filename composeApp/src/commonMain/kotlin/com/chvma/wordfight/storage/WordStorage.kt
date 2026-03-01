package com.chvma.wordfight.storage

import com.chvma.wordfight.model.WordContent

interface WordStorage {
    suspend fun saveWord(word: WordContent)
    suspend fun getAllWords(): List<WordContent>
    suspend fun deleteWord(word: String)
    suspend fun isWordSaved(word: String): Boolean
    suspend fun getBestScore(): Int
    suspend fun saveBestScore(score: Int)
}

fun createWordStorage(): WordStorage {
    return WordStorageImpl(createPreferencesStorage())
}
