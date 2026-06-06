package com.chvma.wordfight.model

data class GameState(
    val score: Int = 0,
    val lives: Int = 3,
    val revivesUsed: Int = 0,
    val pausesUsed: Int = 0,
    val activeCards: List<Card> = emptyList(),
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val level: Int = 1,
    val bestScore: Int = 0,
    val lastMissedWord: WordContent? = null,
    val lastMissedToken: Int = 0,
    val lastMatchedWord: WordContent? = null,
    val lastMatchedToken: Int = 0,
    // True when a finite (limited) session ended with every word guessed.
    val won: Boolean = false,
)
