package com.chvma.wordfight.localization

import com.chvma.wordfight.model.WordCategory

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
    val playSavedWords: String,
    val playSavedWordsHint: String,
    val categories: String,
    val categoriesHint: String,
    val allTopics: String,
    val difficultyAll: String,
    val languages: String,
    val gameOver: String,
    val completed: String,
    val notRankedNote: String,
    val allWordsDone: String,
    val missedWords: String,
    val noSavedWords: String,
    val home: String,
    val playAgain: String,
    val selectLanguage: String,
    val reviveLifeTitle: String,
    val reviveLifeMessage: String,
    val watchVideo: String,
    val noThanks: String,
    val leaderboard: String,
    val leaderboardToday: String,
    val leaderboardAllTime: String,
    val leaderboardLoading: String,
    val leaderboardEmpty: String,
    val leaderboardLanguage: String,
    val onboardingTitle: String,
    val onboardingNameLabel: String,
    val onboardingNamePlaceholder: String,
    val onboardingLanguageLabel: String,
    val onboardingConfirm: String,
)

object Localization {
    /** Localised display name for a word [category]. */
    fun categoryName(category: WordCategory, language: AppLanguage): String = when (category) {
        WordCategory.ANIMALS -> when (language) {
            AppLanguage.EN -> "Animals"; AppLanguage.RU -> "Животные"; AppLanguage.UA -> "Тварини"
            AppLanguage.FR -> "Animaux"; AppLanguage.ES -> "Animales"
        }
        WordCategory.FRUIT_VEG -> when (language) {
            AppLanguage.EN -> "Fruit & veg"; AppLanguage.RU -> "Фрукты и овощи"; AppLanguage.UA -> "Фрукти й овочі"
            AppLanguage.FR -> "Fruits & légumes"; AppLanguage.ES -> "Frutas y verduras"
        }
        WordCategory.FOOD -> when (language) {
            AppLanguage.EN -> "Food & drink"; AppLanguage.RU -> "Еда и напитки"; AppLanguage.UA -> "Їжа й напої"
            AppLanguage.FR -> "Nourriture"; AppLanguage.ES -> "Comida y bebida"
        }
        WordCategory.TRANSPORT -> when (language) {
            AppLanguage.EN -> "Transport"; AppLanguage.RU -> "Транспорт"; AppLanguage.UA -> "Транспорт"
            AppLanguage.FR -> "Transport"; AppLanguage.ES -> "Transporte"
        }
        WordCategory.HOME -> when (language) {
            AppLanguage.EN -> "Home & objects"; AppLanguage.RU -> "Дом и вещи"; AppLanguage.UA -> "Дім і речі"
            AppLanguage.FR -> "Maison & objets"; AppLanguage.ES -> "Casa y objetos"
        }
        WordCategory.TOOLS -> when (language) {
            AppLanguage.EN -> "Tools"; AppLanguage.RU -> "Инструменты"; AppLanguage.UA -> "Інструменти"
            AppLanguage.FR -> "Outils"; AppLanguage.ES -> "Herramientas"
        }
        WordCategory.SCHOOL -> when (language) {
            AppLanguage.EN -> "School & office"; AppLanguage.RU -> "Школа и офис"; AppLanguage.UA -> "Школа й офіс"
            AppLanguage.FR -> "École & bureau"; AppLanguage.ES -> "Escuela y oficina"
        }
        WordCategory.ELECTRONICS -> when (language) {
            AppLanguage.EN -> "Electronics"; AppLanguage.RU -> "Электроника"; AppLanguage.UA -> "Електроніка"
            AppLanguage.FR -> "Électronique"; AppLanguage.ES -> "Electrónica"
        }
        WordCategory.MUSIC -> when (language) {
            AppLanguage.EN -> "Instruments"; AppLanguage.RU -> "Инструменты (муз.)"; AppLanguage.UA -> "Інструменти (муз.)"
            AppLanguage.FR -> "Instruments"; AppLanguage.ES -> "Instrumentos"
        }
        WordCategory.CLOTHING -> when (language) {
            AppLanguage.EN -> "Clothing"; AppLanguage.RU -> "Одежда"; AppLanguage.UA -> "Одяг"
            AppLanguage.FR -> "Vêtements"; AppLanguage.ES -> "Ropa"
        }
        WordCategory.SPORTS -> when (language) {
            AppLanguage.EN -> "Sports"; AppLanguage.RU -> "Спорт"; AppLanguage.UA -> "Спорт"
            AppLanguage.FR -> "Sports"; AppLanguage.ES -> "Deportes"
        }
        WordCategory.NATURE -> when (language) {
            AppLanguage.EN -> "Nature & misc"; AppLanguage.RU -> "Природа и разное"; AppLanguage.UA -> "Природа й інше"
            AppLanguage.FR -> "Nature & divers"; AppLanguage.ES -> "Naturaleza y otros"
        }
        WordCategory.OTHER -> when (language) {
            AppLanguage.EN -> "Other"; AppLanguage.RU -> "Другое"; AppLanguage.UA -> "Інше"
            AppLanguage.FR -> "Autres"; AppLanguage.ES -> "Otros"
        }
    }

    fun strings(language: AppLanguage): AppStrings {
        return when (language) {
            AppLanguage.EN -> AppStrings(
                appTitle = "Pronounce Word!",
                bestLabel = "Best",
                scoreLabel = "Score",
                tapToStart = "Tap to start!",
                myWords = "My words",
                playSavedWords = "Play",
                playSavedWordsHint = "The game will use only the words you saved for review.",
                categories = "Topics",
                categoriesHint = "Pick a topic and difficulty to practise.",
                allTopics = "All topics",
                difficultyAll = "All",
                languages = "Languages",
                gameOver = "Game Over",
                completed = "Done!",
                notRankedNote = "Practice result — not counted in the rating",
                allWordsDone = "All words in this set are done! 🎉",
                missedWords = "Missed words",
                noSavedWords = "No saved words yet",
                home = "Home",
                playAgain = "Play Again",
                selectLanguage = "Select language",
                reviveLifeTitle = "Restore one life?",
                reviveLifeMessage = "Watch a rewarded video to continue.",
                watchVideo = "Watch video",
                noThanks = "No, results",
                leaderboard = "Rating",
                leaderboardToday = "Today",
                leaderboardAllTime = "All time",
                leaderboardLoading = "Loading...",
                leaderboardEmpty = "No players yet",
                leaderboardLanguage = "Language",
                onboardingTitle = "How should we call you in the rating?",
                onboardingNameLabel = "Name",
                onboardingNamePlaceholder = "Enter your name",
                onboardingLanguageLabel = "Language",
                onboardingConfirm = "Continue",
            )
            AppLanguage.RU -> AppStrings(
                appTitle = "Произнеси слово!",
                bestLabel = "Ваш лучший результат",
                scoreLabel = "Счет",
                tapToStart = "Нажми, чтобы начать!",
                myWords = "Мои слова",
                playSavedWords = "Играть",
                playSavedWordsHint = "Игра будет только по словам, которые вы сохранили для повторения.",
                categories = "Темы",
                categoriesHint = "Выберите тему и сложность для тренировки.",
                allTopics = "Все темы",
                difficultyAll = "Все",
                languages = "Языки",
                gameOver = "Игра окончена",
                completed = "Готово!",
                notRankedNote = "Тренировка — результат не идёт в рейтинг",
                allWordsDone = "Все слова в этом наборе пройдены! 🎉",
                missedWords = "Пропущенные слова",
                noSavedWords = "Сохраненных слов пока нет",
                home = "Главная",
                playAgain = "Играть снова",
                selectLanguage = "Выберите язык",
                reviveLifeTitle = "Хотите восстановить одну жизнь?",
                reviveLifeMessage = "Посмотрите рекламное видео и продолжите игру.",
                watchVideo = "Посмотреть видео",
                noThanks = "Нет, результат",
                leaderboard = "Рейтинг",
                leaderboardToday = "За день",
                leaderboardAllTime = "За все время",
                leaderboardLoading = "Загрузка...",
                leaderboardEmpty = "Пока нет игроков",
                leaderboardLanguage = "Язык",
                onboardingTitle = "Как вас назвать в рейтинге?",
                onboardingNameLabel = "Имя",
                onboardingNamePlaceholder = "Введите имя",
                onboardingLanguageLabel = "Язык",
                onboardingConfirm = "Продолжить",
            )
            AppLanguage.UA -> AppStrings(
                appTitle = "Назви слово!",
                bestLabel = "Найкращий",
                scoreLabel = "Рахунок",
                tapToStart = "Натисни, щоб почати!",
                myWords = "Мої слова",
                playSavedWords = "Грати",
                playSavedWordsHint = "Гра буде лише за словами, які ви зберегли для повторення.",
                categories = "Теми",
                categoriesHint = "Оберіть тему та складність для тренування.",
                allTopics = "Усі теми",
                difficultyAll = "Усі",
                languages = "Мови",
                gameOver = "Гру завершено",
                completed = "Готово!",
                notRankedNote = "Тренування — результат не йде в рейтинг",
                allWordsDone = "Усі слова в цьому наборі пройдено! 🎉",
                missedWords = "Пропущені слова",
                noSavedWords = "Збережених слів поки немає",
                home = "Головна",
                playAgain = "Грати знову",
                selectLanguage = "Оберіть мову",
                reviveLifeTitle = "Хочете відновити одне життя?",
                reviveLifeMessage = "Перегляньте рекламне відео та продовжуйте гру.",
                watchVideo = "Переглянути відео",
                noThanks = "Ні, результат",
                leaderboard = "Рейтинг",
                leaderboardToday = "За день",
                leaderboardAllTime = "За весь час",
                leaderboardLoading = "Завантаження...",
                leaderboardEmpty = "Поки немає гравців",
                leaderboardLanguage = "Мова",
                onboardingTitle = "Як вас називати в рейтингу?",
                onboardingNameLabel = "Ім'я",
                onboardingNamePlaceholder = "Введіть ім'я",
                onboardingLanguageLabel = "Мова",
                onboardingConfirm = "Продовжити",
            )
            AppLanguage.FR -> AppStrings(
                appTitle = "Prononcer le mot!",
                bestLabel = "Meilleur",
                scoreLabel = "Score",
                tapToStart = "Appuie pour commencer !",
                myWords = "Mes mots",
                playSavedWords = "Jouer",
                playSavedWordsHint = "Le jeu utilisera uniquement les mots que vous avez enregistrés pour révision.",
                categories = "Thèmes",
                categoriesHint = "Choisissez un thème et une difficulté.",
                allTopics = "Tous les thèmes",
                difficultyAll = "Tous",
                languages = "Langues",
                gameOver = "Fin de partie",
                completed = "Terminé !",
                notRankedNote = "Entraînement — non compté dans le classement",
                allWordsDone = "Tous les mots de cet ensemble sont faits ! 🎉",
                missedWords = "Mots manqués",
                noSavedWords = "Aucun mot enregistré pour l'instant",
                home = "Accueil",
                playAgain = "Rejouer",
                selectLanguage = "Choisir la langue",
                reviveLifeTitle = "Récupérer une vie ?",
                reviveLifeMessage = "Regardez une vidéo récompensée pour continuer.",
                watchVideo = "Voir la vidéo",
                noThanks = "Non, résultat",
                leaderboard = "Classement",
                leaderboardToday = "Aujourd'hui",
                leaderboardAllTime = "Global",
                leaderboardLoading = "Chargement...",
                leaderboardEmpty = "Aucun joueur",
                leaderboardLanguage = "Langue",
                onboardingTitle = "Comment vous appeler dans le classement ?",
                onboardingNameLabel = "Nom",
                onboardingNamePlaceholder = "Entrez votre nom",
                onboardingLanguageLabel = "Langue",
                onboardingConfirm = "Continuer",
            )
            AppLanguage.ES -> AppStrings(
                appTitle = "Pronunciar palabra!",
                bestLabel = "Mejor",
                scoreLabel = "Puntuación",
                tapToStart = "Toca para empezar!",
                myWords = "Mis palabras",
                playSavedWords = "Jugar",
                playSavedWordsHint = "El juego usará solo las palabras que guardaste para repasar.",
                categories = "Temas",
                categoriesHint = "Elige un tema y una dificultad para practicar.",
                allTopics = "Todos los temas",
                difficultyAll = "Todos",
                languages = "Idiomas",
                gameOver = "Fin del juego",
                completed = "¡Listo!",
                notRankedNote = "Práctica — no cuenta para la clasificación",
                allWordsDone = "¡Todas las palabras de este conjunto están hechas! 🎉",
                missedWords = "Palabras perdidas",
                noSavedWords = "Aún no hay palabras guardadas",
                home = "Inicio",
                playAgain = "Jugar de nuevo",
                selectLanguage = "Elige idioma",
                reviveLifeTitle = "¿Recuperar una vida?",
                reviveLifeMessage = "Mira un video con recompensa para continuar.",
                watchVideo = "Ver video",
                noThanks = "No, resultado",
                leaderboard = "Clasificacion",
                leaderboardToday = "Hoy",
                leaderboardAllTime = "Historico",
                leaderboardLoading = "Cargando...",
                leaderboardEmpty = "Aun no hay jugadores",
                leaderboardLanguage = "Idioma",
                onboardingTitle = "¿Como te llamamos en la clasificacion?",
                onboardingNameLabel = "Nombre",
                onboardingNamePlaceholder = "Ingresa tu nombre",
                onboardingLanguageLabel = "Idioma",
                onboardingConfirm = "Continuar",
            )
        }
    }
}
