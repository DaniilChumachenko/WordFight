package com.chvma.wordfight.model

import com.chvma.wordfight.localization.AppLanguage
import org.jetbrains.compose.resources.DrawableResource

data class WordContent(
    val id: Int,
    val word: String,
    val translations: List<Translation>,
    val level: Int = 1,
    val res: DrawableResource,
    val category: WordCategory = WordCategory.OTHER,
) {
    fun translationFor(language: AppLanguage): String {
        val translation = translations.firstOrNull() ?: return word
        return translation.textFor(language)
    }
}

data class Translation(
    val originalPhrase: String,
    val ru: String,
    val ua: String,
    val es: String,
    val fr: String,
) {
    fun textFor(language: AppLanguage): String {
        return when (language) {
            AppLanguage.EN -> originalPhrase
            AppLanguage.RU -> ru
            AppLanguage.UA -> ua
            AppLanguage.ES -> es
            AppLanguage.FR -> fr
        }
    }
}
