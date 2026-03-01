package com.chvma.wordfight.model

data class Card(
    val id: String,
    val imageKey: Int,
    val word: String,
    val translation: String,
    val level: Int,
    val x: Float,      // 0..1 normalized
    val y: Float,      // 0..1 normalized (0=top, 1=bottom)
    val speed: Float,  // units per second (normalized)
)
