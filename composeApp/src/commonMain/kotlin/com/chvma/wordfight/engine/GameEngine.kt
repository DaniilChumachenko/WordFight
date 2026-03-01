package com.chvma.wordfight.engine

import com.chvma.wordfight.content.WordRepository
import com.chvma.wordfight.model.Card
import com.chvma.wordfight.model.GameState
import com.chvma.wordfight.model.WordContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

class GameEngine(
    private val wordMatcher: WordMatcher = WordMatcher(),
) {
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state

    private var timeSinceLastSpawn = 0f
    private var spawnInterval = 9f
    private var cardIdCounter = 0
    private var bestScore = 0
    private var shuffledPool: ArrayDeque<WordContent> = ArrayDeque()

    fun update(deltaTime: Float, screenWidth: Float, screenHeight: Float) {
        if (screenWidth == 0f || screenHeight == 0f) return
        val current = _state.value
        if (current.isGameOver || current.isPaused) return

        // Move cards down
        val moved = current.activeCards.map { card ->
            card.copy(y = card.y + card.speed * deltaTime)
        }

        // Cards that fell off screen
        val fallen = moved.filter { it.y >= 1f }
        val alive = moved.filter { it.y < 1f }

        var lives = current.lives - fallen.size
        val isGameOver = lives <= 0
        if (isGameOver) lives = 0

        // Spawn new cards
        timeSinceLastSpawn += deltaTime
        val updatedCards = alive.toMutableList()
        if (timeSinceLastSpawn >= spawnInterval && !isGameOver) {
            if (shuffledPool.isEmpty()) {
                shuffledPool = ArrayDeque(WordRepository.forLevel(current.level).shuffled())
            }
            if (shuffledPool.isNotEmpty()) {
                val word = shuffledPool.removeFirst()
                val baseSpeed = 0.06f + (current.level - 1) * 0.007f
                updatedCards.add(
                    Card(
                        id = "card_${cardIdCounter++}",
                        imageKey = word.imageKey,
                        word = word.word,
                        translation = word.translation,
                        x = Random.nextFloat() * 0.7f + 0.15f,
                        y = -0.05f,
                        speed = baseSpeed + Random.nextFloat() * 0.02f,
                    )
                )
            }
            timeSinceLastSpawn = 0f
            spawnInterval = maxOf(4.5f, 9f - current.score * 0.05f)
        }

        // Level progression
        val newLevel = when {
            current.score >= 30 -> 3
            current.score >= 15 -> 2
            else -> 1
        }

        if (isGameOver && current.score > bestScore) {
            bestScore = current.score
        }

        _state.value = current.copy(
            activeCards = updatedCards,
            lives = lives,
            isGameOver = isGameOver,
            level = newLevel,
            bestScore = if (current.score > bestScore) current.score else bestScore,
        )
    }

    fun tryMatch(spoken: String): Boolean {
        val current = _state.value
        if (current.isGameOver || current.isPaused) return false
        val matched = current.activeCards.firstOrNull { wordMatcher.matches(spoken, it.word) }
        if (matched != null) {
            val newScore = current.score + 1
            if (newScore > bestScore) bestScore = newScore
            _state.value = current.copy(
                activeCards = current.activeCards - matched,
                score = newScore,
                bestScore = bestScore,
            )
            return true
        }
        return false
    }

    fun pause() {
        _state.value = _state.value.copy(isPaused = true)
    }

    fun resume() {
        _state.value = _state.value.copy(isPaused = false)
    }

    fun restart() {
        timeSinceLastSpawn = 0f
        spawnInterval = 9f
        cardIdCounter = 0
        shuffledPool = ArrayDeque()
        _state.value = GameState(bestScore = bestScore)
    }
}
