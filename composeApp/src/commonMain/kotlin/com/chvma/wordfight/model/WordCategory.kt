package com.chvma.wordfight.model

/**
 * Thematic groups the word catalogue is organised into. Used to let players
 * practise a single topic (and optionally a single difficulty [WordContent.level])
 * instead of the whole catalogue. Display names are resolved per-language by
 * [com.chvma.wordfight.localization.Localization.categoryName].
 */
enum class WordCategory(val emoji: String) {
    ANIMALS("🐾"),
    FRUIT_VEG("🍎"),
    FOOD("🍔"),
    TRANSPORT("🚗"),
    HOME("🏠"),
    TOOLS("🔧"),
    SCHOOL("✏️"),
    ELECTRONICS("💻"),
    MUSIC("🎸"),
    CLOTHING("👕"),
    SPORTS("⚽"),
    NATURE("🌿"),
    OTHER("✨"),
    ;

    companion object {
        /** Categories offered on the selection screen (excludes the [OTHER] fallback). */
        val selectable: List<WordCategory> = entries.filter { it != OTHER }
    }
}
