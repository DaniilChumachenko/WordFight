package com.chvma.wordfight.model

data class Card(
    val id: String,
    val content: WordContent,
    val x: Float,      // 0..1 normalized
    val y: Float,      // 0..1 normalized (0=top, 1=bottom)
    val speed: Float,  // units per second (normalized)
)
