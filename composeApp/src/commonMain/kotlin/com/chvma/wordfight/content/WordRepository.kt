package com.chvma.wordfight.content

import com.chvma.wordfight.model.WordContent

object WordRepository {

    val words: List<WordContent> = listOf(
        WordContent(1, "suitcase", "чемодан", 1),
        WordContent(2, "newspaper", "газета", 1),
        WordContent(3, "camera", "фотоаппарат", 1),
        WordContent(4, "bridge", "мост", 1),
        WordContent(5, "stairs", "лестница", 1),
        WordContent(6, "umbrella", "зонт", 1),
        WordContent(7, "wallet", "бумажник", 1),
        WordContent(8, "mirror", "зеркало", 1),
        WordContent(9, "pillow", "подушка", 1),
        // Add more entries here as you add images (10.png, 11.png, ...)
    )

    fun forLevel(level: Int): List<WordContent> = words.filter { it.level <= level }
}
