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
    companion object {
        const val MAX_REVIVES_PER_GAME = 3
        const val MAX_PAUSES_PER_GAME = 3
    }

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state

    private var timeSinceLastSpawn = 0f
    private var spawnInterval = 6f
    private var timeSinceSpeedIncrease = 0f
    private var speedBonus = 0f
    private var cardIdCounter = 0
    private var bestScore = 0
    private var wordPool: List<WordContent> = WordRepository.words
    private var limited = false
    private var shuffledPool: ArrayDeque<WordContent> = ArrayDeque()
    private var recentlyShown: List<WordContent> = emptyList()
    private val missedWords = mutableListOf<WordContent>()
    private var missToken = 0
    private var matchToken = 0
    private var fallSpeedScale = 1f
    private var spawnSpeedScale = 1f
    private var processingSlowdown = false

    init {
        timeSinceLastSpawn = spawnInterval
    }

    fun update(deltaTime: Float, screenWidth: Float, screenHeight: Float) {
        if (screenWidth == 0f || screenHeight == 0f) return
        val current = _state.value
        if (current.isGameOver || current.isPaused) return

        // Move cards down
        val movementDelta = deltaTime * fallSpeedScale
        val moved = current.activeCards.map { card ->
            val levelScale = if (processingSlowdown) levelFallScale(card.content.level) else 1f
            card.copy(y = card.y + card.speed * movementDelta * levelScale)
        }

        // Cards that fell off screen
        val fallen = moved.filter { it.y >= 1f }
        val alive = moved.filter { it.y < 1f }

        // Track missed words
        var lastFallenWord: WordContent? = null
        fallen.forEach { card ->
            val wordContent = card.content
            lastFallenWord = wordContent
            if (!missedWords.any { it.id == card.content.id }) {
                missedWords.add(wordContent)
            }
        }
        val lastMissedToken = if (lastFallenWord != null) {
            missToken += 1
            missToken
        } else {
            current.lastMissedToken
        }

        var lives = current.lives - fallen.size
        val lostAllLives = lives <= 0
        if (lostAllLives) lives = 0

        // Spawn new cards
        timeSinceLastSpawn += deltaTime * spawnSpeedScale
        timeSinceSpeedIncrease += deltaTime
        while (timeSinceSpeedIncrease >= 20f) {
            speedBonus += 0.02f
            timeSinceSpeedIncrease -= 20f
        }
        val updatedCards = alive.toMutableList()
        if (timeSinceLastSpawn >= spawnInterval && !lostAllLives) {
            // Infinite modes refill the pool endlessly; limited modes play each
            // word once and let the pool run dry (see completion check below).
            if (shuffledPool.isEmpty() && !limited) {
                val allWords = wordPool
                val recentSet = recentlyShown.toSet()
                // Put recently shown words at the end so they don't repeat right away
                val fresh = allWords.filter { it !in recentSet }.shuffled()
                val deferred = recentlyShown.shuffled()
                shuffledPool = ArrayDeque(fresh + deferred)
                recentlyShown = emptyList()
            }
            if (shuffledPool.isNotEmpty()) {
                val word = shuffledPool.removeFirst()
                recentlyShown = (recentlyShown + word).takeLast(5)
                val baseSpeed = 0.2f + speedBonus
                updatedCards.add(
                    Card(
                        id = "card_${cardIdCounter++}",
                        content = word,
                        x = Random.nextFloat() * 0.7f + 0.15f,
                        y = -0.05f,
                        speed = baseSpeed + Random.nextFloat() * 0.02f,
                    )
                )
            }
        timeSinceLastSpawn = 0f
        spawnInterval = maxOf(3f, 6f - current.score * 0.05f)
        }

        // Limited mode finishes once every word has been resolved (matched or
        // missed) and nothing is left on screen — even with lives remaining.
        val completed = limited && shuffledPool.isEmpty() && updatedCards.isEmpty()
        val isGameOver = lostAllLives || completed
        // A "win" is a finite session finished with no missed words.
        val won = completed && missedWords.isEmpty()

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
            won = won,
            level = newLevel,
            bestScore = if (current.score > bestScore) current.score else bestScore,
            lastMissedWord = lastFallenWord ?: current.lastMissedWord,
            lastMissedToken = lastMissedToken,
        )
    }

    fun tryMatch(spoken: String): Boolean {
        val current = _state.value
        if (current.isGameOver || current.isPaused) {
            logRecognition(
                "Ignored transcript=\"$spoken\": gameOver=${current.isGameOver}, paused=${current.isPaused}",
            )
            return false
        }
        val expectedWords = current.activeCards.map { it.content.word }
        logRecognition("Trying transcript=\"$spoken\" against activeWords=$expectedWords")
        val evaluations = current.activeCards.map { card ->
            card to wordMatcher.evaluate(spoken, card.content.word)
        }
        evaluations.forEach { (card, result) ->
            logRecognition("Candidate target=\"${card.content.word}\": ${result.toLogString()}")
        }
        val matched = evaluations.firstOrNull { (_, result) -> result.matched }?.first
        if (matched != null) {
            logRecognition("Accepted transcript=\"$spoken\" as target=\"${matched.content.word}\"")
            val newScore = current.score + 1
            if (newScore > bestScore) bestScore = newScore
            val remaining = current.activeCards - matched
            if (remaining.isEmpty()) {
                // Screen is empty — speed up next spawn to ~2 seconds from now
                timeSinceLastSpawn = (spawnInterval - 2f).coerceIn(0f, spawnInterval)
            }
            matchToken += 1
            _state.value = current.copy(
                activeCards = remaining,
                score = newScore,
                bestScore = bestScore,
                lastMatchedWord = matched.content,
                lastMatchedToken = matchToken,
            )
            return true
        }
        logRecognition("Rejected transcript=\"$spoken\": no active word matched")
        return false
    }

    fun pause() {
        _state.value = _state.value.copy(isPaused = true)
    }

    /**
     * Consumes one of the limited pauses. Unlike [canUsePause], an already
     * paused game is allowed here: the game screen freezes the engine with a
     * plain [pause] while the rewarded ad is on top and then converts that
     * hold into the earned pause via this method.
     */
    fun pauseWithLimit(): Boolean {
        val current = _state.value
        if (current.isGameOver || current.pausesUsed >= MAX_PAUSES_PER_GAME) return false
        _state.value = current.copy(
            isPaused = true,
            pausesUsed = current.pausesUsed + 1,
        )
        return true
    }

    fun resume() {
        _state.value = _state.value.copy(isPaused = false)
    }

    fun reviveOneLife(): Boolean {
        val current = _state.value
        if (!canRevive()) return false
        _state.value = current.copy(
            lives = 1,
            revivesUsed = current.revivesUsed + 1,
            isGameOver = false,
            isPaused = false,
        )
        return true
    }

    fun canRevive(): Boolean {
        val current = _state.value
        return current.isGameOver && current.lives <= 0 && current.revivesUsed < MAX_REVIVES_PER_GAME
    }

    fun canUsePause(): Boolean {
        val current = _state.value
        return !current.isGameOver && !current.isPaused && current.pausesUsed < MAX_PAUSES_PER_GAME
    }

    fun getMissedWords(): List<WordContent> {
        return missedWords.toList()
    }

    /**
     * Swaps the pool of words the game spawns from (e.g. the player's saved
     * words for a focused review session). Falls back to the full catalogue when
     * given an empty list. When [limited] is true the game plays each word once
     * and ends when the pool is exhausted (finite session); otherwise it spawns
     * endlessly. Call [restart] afterwards to start a clean game.
     */
    fun setWordPool(words: List<WordContent>, limited: Boolean = false) {
        wordPool = words.ifEmpty { WordRepository.words }
        this.limited = limited
        resetPool()
    }

    /** Rebuilds the spawn pool: pre-filled (shuffled) for limited mode, empty
     *  (filled on demand) for endless mode. */
    private fun resetPool() {
        shuffledPool = if (limited) ArrayDeque(wordPool.shuffled()) else ArrayDeque()
        recentlyShown = emptyList()
    }

    fun restart() {
        spawnInterval = 6f
        timeSinceSpeedIncrease = 0f
        speedBonus = 0f
        // Spawn first card immediately after restart
        timeSinceLastSpawn = spawnInterval
        cardIdCounter = 0
        resetPool()
        missedWords.clear()
        missToken = 0
        matchToken = 0
        _state.value = GameState(bestScore = bestScore)
    }

    fun setSpeedScales(fall: Float, spawn: Float) {
        fallSpeedScale = fall.coerceAtLeast(0f)
        spawnSpeedScale = spawn.coerceAtLeast(0f)
    }

    fun setProcessingSlowdown(enabled: Boolean) {
        processingSlowdown = enabled
    }

    private fun levelFallScale(level: Int): Float {
        return when (level) {
            1 -> 0.9f   // 10% slower during speech
            2 -> 0.8f   // 20% slower during speech
            else -> 0.7f // 30% slower during speech
        }
    }
}
