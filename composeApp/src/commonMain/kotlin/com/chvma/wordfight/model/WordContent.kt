package com.chvma.wordfight.model

data class WordContent(
    val imageKey: Int,
    val word: String,
    val translation: String,
    val level: Int = 1,
)
