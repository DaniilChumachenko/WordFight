package com.chvma.wordfight.model

data class GameState(
    val score: Int = 0,
    val lives: Int = 3,
    val activeCards: List<Card> = emptyList(),
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val level: Int = 1,
    val bestScore: Int = 0,
)
