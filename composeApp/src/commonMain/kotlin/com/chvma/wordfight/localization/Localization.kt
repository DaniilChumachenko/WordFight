package com.chvma.wordfight.localization

enum class AppLanguage(
    val code: String,
    val nativeName: String,
) {
    EN("en", "English"),
    RU("ru", "Русский"),
    UA("uk", "Українська"),
    FR("fr", "Français"),
    ES("es", "Español"),
    ;

    companion object {
        val supported = listOf(EN, RU, UA, FR, ES)

        fun fromCode(code: String?): AppLanguage {
            return supported.firstOrNull { it.code == code } ?: EN
        }
    }
}

data class AppStrings(
    val appTitle: String,
    val bestLabel: String,
    val scoreLabel: String,
    val tapToStart: String,
    val myWords: String,
    val languages: String,
    val gameOver: String,
    val missedWords: String,
    val noSavedWords: String,
    val home: String,
    val playAgain: String,
    val selectLanguage: String,
)

object Localization {
    fun strings(language: AppLanguage): AppStrings {
        return when (language) {
            AppLanguage.EN -> AppStrings(
                appTitle = "Pronounce Word!",
                bestLabel = "Best",
                scoreLabel = "Score",
                tapToStart = "Tap to start!",
                myWords = "My words",
                languages = "Languages",
                gameOver = "Game Over",
                missedWords = "Missed words",
                noSavedWords = "No saved words yet",
                home = "Home",
                playAgain = "Play Again",
                selectLanguage = "Select language",
            )
            AppLanguage.RU -> AppStrings(
                appTitle = "Произнеси слово!",
                bestLabel = "Лучший",
                scoreLabel = "Счет",
                tapToStart = "Нажми, чтобы начать!",
                myWords = "Мои слова",
                languages = "Языки",
                gameOver = "Игра окончена",
                missedWords = "Пропущенные слова",
                noSavedWords = "Сохраненных слов пока нет",
                home = "Главная",
                playAgain = "Играть снова",
                selectLanguage = "Выберите язык",
            )
            AppLanguage.UA -> AppStrings(
                appTitle = "Назви слово!",
                bestLabel = "Найкращий",
                scoreLabel = "Рахунок",
                tapToStart = "Натисни, щоб почати!",
                myWords = "Мої слова",
                languages = "Мови",
                gameOver = "Гру завершено",
                missedWords = "Пропущені слова",
                noSavedWords = "Збережених слів поки немає",
                home = "Головна",
                playAgain = "Грати знову",
                selectLanguage = "Оберіть мову",
            )
            AppLanguage.FR -> AppStrings(
                appTitle = "Prononcer le mot!",
                bestLabel = "Meilleur",
                scoreLabel = "Score",
                tapToStart = "Appuie pour commencer !",
                myWords = "Mes mots",
                languages = "Langues",
                gameOver = "Fin de partie",
                missedWords = "Mots manqués",
                noSavedWords = "Aucun mot enregistré pour l'instant",
                home = "Accueil",
                playAgain = "Rejouer",
                selectLanguage = "Choisir la langue",
            )
            AppLanguage.ES -> AppStrings(
                appTitle = "Pronunciar palabra!",
                bestLabel = "Mejor",
                scoreLabel = "Puntuación",
                tapToStart = "Toca para empezar!",
                myWords = "Mis palabras",
                languages = "Idiomas",
                gameOver = "Fin del juego",
                missedWords = "Palabras perdidas",
                noSavedWords = "Aún no hay palabras guardadas",
                home = "Inicio",
                playAgain = "Jugar de nuevo",
                selectLanguage = "Elige idioma",
            )
        }
    }
}
