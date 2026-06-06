package com.chvma.wordfight.haptics

enum class HapticType {
    Correct,
    LifeLost,
    GameOver,
}

interface HapticEngine {
    fun perform(type: HapticType)
}

expect fun createHapticEngine(): HapticEngine
