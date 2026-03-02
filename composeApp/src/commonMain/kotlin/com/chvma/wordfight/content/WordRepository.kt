package com.chvma.wordfight.content

import com.chvma.wordfight.model.Translation
import com.chvma.wordfight.model.WordContent
import org.jetbrains.compose.resources.DrawableResource
import wordfight.composeapp.generated.resources.Res
import wordfight.composeapp.generated.resources._1
import wordfight.composeapp.generated.resources._2
import wordfight.composeapp.generated.resources._3
import wordfight.composeapp.generated.resources._4
import wordfight.composeapp.generated.resources._5
import wordfight.composeapp.generated.resources._6
import wordfight.composeapp.generated.resources._7
import wordfight.composeapp.generated.resources._8
import wordfight.composeapp.generated.resources._9
import wordfight.composeapp.generated.resources._10
import wordfight.composeapp.generated.resources._11
import wordfight.composeapp.generated.resources._12
import wordfight.composeapp.generated.resources._13
import wordfight.composeapp.generated.resources._14
import wordfight.composeapp.generated.resources._15
import wordfight.composeapp.generated.resources._16
import wordfight.composeapp.generated.resources._17
import wordfight.composeapp.generated.resources._18
import wordfight.composeapp.generated.resources._19
import wordfight.composeapp.generated.resources._20
import wordfight.composeapp.generated.resources._21
import wordfight.composeapp.generated.resources._22
import wordfight.composeapp.generated.resources._23
import wordfight.composeapp.generated.resources._24
import wordfight.composeapp.generated.resources._26
import wordfight.composeapp.generated.resources._27
import wordfight.composeapp.generated.resources._28
import wordfight.composeapp.generated.resources._29
import wordfight.composeapp.generated.resources._30
import wordfight.composeapp.generated.resources._31
import wordfight.composeapp.generated.resources._32
import wordfight.composeapp.generated.resources._33
import wordfight.composeapp.generated.resources._34
import wordfight.composeapp.generated.resources._39
import wordfight.composeapp.generated.resources._40
import wordfight.composeapp.generated.resources._41
import wordfight.composeapp.generated.resources._42
import wordfight.composeapp.generated.resources._43
import wordfight.composeapp.generated.resources._44
import wordfight.composeapp.generated.resources._45
import wordfight.composeapp.generated.resources._46
import wordfight.composeapp.generated.resources._47
import wordfight.composeapp.generated.resources._48
import wordfight.composeapp.generated.resources._49
import wordfight.composeapp.generated.resources._50
import wordfight.composeapp.generated.resources._51
import wordfight.composeapp.generated.resources._52
import wordfight.composeapp.generated.resources._53
import wordfight.composeapp.generated.resources._54
import wordfight.composeapp.generated.resources._55
import wordfight.composeapp.generated.resources._56
import wordfight.composeapp.generated.resources._57
import wordfight.composeapp.generated.resources._58
import wordfight.composeapp.generated.resources._59
import wordfight.composeapp.generated.resources._60
import wordfight.composeapp.generated.resources._61
import wordfight.composeapp.generated.resources._63
import wordfight.composeapp.generated.resources._64
import wordfight.composeapp.generated.resources._65
import wordfight.composeapp.generated.resources._66
import wordfight.composeapp.generated.resources._67
import wordfight.composeapp.generated.resources._68
import wordfight.composeapp.generated.resources._69
import wordfight.composeapp.generated.resources._70
import wordfight.composeapp.generated.resources._71
import wordfight.composeapp.generated.resources._73
import wordfight.composeapp.generated.resources._75
import wordfight.composeapp.generated.resources._76
import wordfight.composeapp.generated.resources._77
import wordfight.composeapp.generated.resources._78
import wordfight.composeapp.generated.resources._79
import wordfight.composeapp.generated.resources._80
import wordfight.composeapp.generated.resources._81
import wordfight.composeapp.generated.resources._82
import wordfight.composeapp.generated.resources._83
import wordfight.composeapp.generated.resources._84
import wordfight.composeapp.generated.resources._85
import wordfight.composeapp.generated.resources._86

object WordRepository {
    val words: List<WordContent> = listOf(
        word(1, "suitcase", "чемодан", "валіза", "maleta", "valise", 1),
        word(2, "newspaper", "газета", "газета", "periódico", "journal", 1),
        word(3, "camera", "фотоаппарат", "фотоапарат", "cámara", "appareil photo", 1),
        word(4, "bridge", "мост", "міст", "puente", "pont", 1),
        word(5, "stairs", "лестница", "сходи", "escaleras", "escaliers", 1),
        word(6, "umbrella", "зонт", "парасолька", "paraguas", "parapluie", 1),
        word(7, "wallet", "бумажник", "гаманець", "billetera", "portefeuille", 1),
        word(8, "mirror", "зеркало", "дзеркало", "espejo", "miroir", 1),
        word(9, "pillow", "подушка", "подушка", "almohada", "oreiller", 1),
        word(10, "knife", "нож", "ніж", "cuchillo", "couteau", 1),
        word(11, "bottle", "бутылка", "пляшка", "botella", "bouteille", 1),
        word(12, "glass", "стакан", "склянка", "vaso", "verre", 1),
        word(13, "water", "вода", "вода", "agua", "eau", 1),
        word(14, "ladder", "стремянка", "драбина", "escalera de mano", "échelle", 1),
        word(15, "letter", "письмо", "лист", "carta", "lettre", 1),
        word(16, "stamp", "марка", "марка", "sello", "timbre", 1),
        word(17, "wheel", "колесо", "колесо", "rueda", "roue", 1),
        word(18, "flag", "флаг", "прапор", "bandera", "drapeau", 1),
        word(19, "key", "ключ", "ключ", "llave", "clé", 1),
        word(20, "brush", "кисть", "пензель", "pincel", "pinceau", 1),
        word(21, "scissors", "ножницы", "ножиці", "tijeras", "ciseaux", 1),
        word(22, "climb", "забираться", "підійматися", "escalar", "grimper", 1),
        word(23, "drop", "ронять", "роняти", "dejar caer", "laisser tomber", 1),
        word(24, "push", "толкать", "штовхати", "empujar", "pousser", 1),
        word(26, "break", "ломать", "ламати", "romper", "casser", 1),
        word(27, "build", "строить", "будувати", "construir", "construire", 2),
        word(28, "cubes", "кубики", "кубики", "cubos", "cubes", 2),
        word(29, "carry", "нести", "нести", "llevar", "porter", 2),
        word(30, "catch", "ловить", "ловити", "atrapar", "attraper", 2),
        word(31, "shelf", "полка", "полиця", "estante", "étagère", 2),
        word(32, "curtain", "штора", "штора", "cortina", "rideau", 2),
        word(33, "carpet", "ковер", "килим", "alfombra", "tapis", 2),
        word(34, "bucket", "ведро", "відро", "cubo", "seau", 2),
        word(39, "comb", "расческа", "гребінець", "peine", "peigne", 2),
        word(40, "toothbrush", "зубная щетка", "зубна щітка", "cepillo de dientes", "brosse à dents", 2),
        word(41, "soap", "мыло", "мило", "jabón", "savon", 2),
        word(42, "iron", "утюг", "праска", "plancha", "fer à repasser", 2),
        word(43, "toaster", "тостер", "тостер", "tostadora", "grille-pain", 2),
        word(44, "kettle", "чайник", "чайник", "hervidor", "bouilloire", 2),
        word(45, "plate", "тарелка", "тарілка", "plato", "assiette", 2),
        word(46, "spoon", "ложка", "ложка", "cuchara", "cuillère", 2),
        word(47, "fork", "вилка", "виделка", "tenedor", "fourchette", 2),
        word(48, "pan", "сковорода", "сковорода", "sartén", "poêle", 2),
        word(51, "oven", "духовка", "духовка", "horno", "four", 2),
        word(52, "washing machine", "стиральная машина", "пральна машина", "lavadora", "machine à laver", 2),
        word(53, "vacuum cleaner", "пылесос", "пилосос", "aspiradora", "aspirateur", 2),
        word(54, "hammer", "молоток", "молоток", "martillo", "marteau", 2),
        word(50, "screwdriver", "отвертка", "викрутка", "destornillador", "tournevis", 2),
        word(49, "nail", "гвоздь", "цвях", "clavo", "clou", 2),
        word(55, "needle", "иголка", "голка", "aguja", "aiguille", 2),
        word(57, "thread", "нитка", "нитка", "hilo", "fil", 2),
        word(56, "button", "пуговица", "ґудзик", "botón", "bouton", 2),
        word(58, "coin", "монета", "монета", "moneda", "pièce", 2),
        word(59, "battery", "батарейка", "батарейка", "pila", "pile", 2),
        word(60, "candle", "свеча", "свічка", "vela", "bougie", 2),
        word(61, "match", "спичка", "сірник", "fósforo", "allumette", 3),
        word(63, "clock", "часы", "годинник", "reloj", "horloge", 3),
        word(64, "calendar", "календарь", "календар", "calendario", "calendrier", 3),
        word(65, "diary", "дневник", "щоденник", "diario", "journal", 3),
        word(66, "notebook", "тетрадь", "зошит", "cuaderno", "cahier", 3),
        word(67, "pencil", "карандаш", "олівець", "lápiz", "crayon", 3),
        word(68, "ruler", "линейка", "лінійка", "regla", "règle", 3),
        word(69, "eraser", "ластик", "гумка", "goma de borrar", "gomme", 3),
        word(70, "sharpener", "точилка", "точилка", "sacapuntas", "taille-crayon", 3),
        word(76, "compass", "компас", "компас", "brújula", "boussole", 3),
        word(71, "backpack", "рюкзак", "рюкзак", "mochila", "sac à dos", 3),
        word(73, "balloon", "воздушный шар", "повітряна куля", "globo", "ballon", 3),
        word(75, "tent", "палатка", "намет", "tienda de campaña", "tente", 3),
        word(77, "flashlight", "фонарик", "ліхтарик", "linterna", "lampe torche", 3),
        word(78, "binoculars", "бинокль", "бінокль", "prismáticos", "jumelles", 3),
        word(81, "life jacket", "спасательный жилет", "рятувальний жилет", "chaleco salvavidas", "gilet de sauvetage", 3),
        word(82, "anchor", "якорь", "якір", "ancla", "ancre", 3),
        word(74, "paddle", "весло", "весло", "remo", "pagaie", 3),
        word(79, "fishing rod", "удочка", "вудка", "caña de pescar", "canne à pêche", 3),
        word(80, "net", "сеть", "сітка", "red", "filet", 3),
        word(83, "helmet", "шлем", "шолом", "casco", "casque", 3),
        word(84, "goggles", "защитные очки", "захисні окуляри", "gafas protectoras", "lunettes de protection", 3),
        word(85, "gloves", "перчатки", "рукавички", "guantes", "gants", 3),
        word(86, "boots", "ботинки", "черевики", "botas", "bottes", 3),
    )

    private val wordsById: Map<Int, WordContent> = words.associateBy { it.id }
    private val wordsByWord: Map<String, WordContent> = words.associateBy { it.word }

    fun byId(id: Int): WordContent? = wordsById[id]

    fun byWord(word: String): WordContent? = wordsByWord[word]

    private fun word(
        id: Int,
        text: String,
        ru: String,
        ua: String,
        es: String,
        fr: String,
        level: Int,
    ): WordContent {
        return WordContent(
            id = id,
            word = text,
            translations = listOf(
                Translation(
                    originalPhrase = text,
                    ru = ru,
                    ua = ua,
                    es = es,
                    fr = fr,
                )
            ),
            level = level,
            res = resFor(id),
        )
    }

    private fun resFor(id: Int): DrawableResource = when (id) {
        1 -> Res.drawable._1
        2 -> Res.drawable._2
        3 -> Res.drawable._3
        4 -> Res.drawable._4
        5 -> Res.drawable._5
        6 -> Res.drawable._6
        7 -> Res.drawable._7
        8 -> Res.drawable._8
        9 -> Res.drawable._9
        10 -> Res.drawable._10
        11 -> Res.drawable._11
        12 -> Res.drawable._12
        13 -> Res.drawable._13
        14 -> Res.drawable._14
        15 -> Res.drawable._15
        16 -> Res.drawable._16
        17 -> Res.drawable._17
        18 -> Res.drawable._18
        19 -> Res.drawable._19
        20 -> Res.drawable._20
        21 -> Res.drawable._21
        22 -> Res.drawable._22
        23 -> Res.drawable._23
        24 -> Res.drawable._24
        26 -> Res.drawable._26
        27 -> Res.drawable._27
        28 -> Res.drawable._28
        29 -> Res.drawable._29
        30 -> Res.drawable._30
        31 -> Res.drawable._31
        32 -> Res.drawable._32
        33 -> Res.drawable._33
        34 -> Res.drawable._34
        39 -> Res.drawable._39
        40 -> Res.drawable._40
        41 -> Res.drawable._41
        42 -> Res.drawable._42
        43 -> Res.drawable._43
        44 -> Res.drawable._44
        45 -> Res.drawable._45
        46 -> Res.drawable._46
        47 -> Res.drawable._47
        48 -> Res.drawable._48
        49 -> Res.drawable._49
        50 -> Res.drawable._50
        51 -> Res.drawable._51
        52 -> Res.drawable._52
        53 -> Res.drawable._53
        54 -> Res.drawable._54
        55 -> Res.drawable._55
        56 -> Res.drawable._56
        57 -> Res.drawable._57
        58 -> Res.drawable._58
        59 -> Res.drawable._59
        60 -> Res.drawable._60
        61 -> Res.drawable._61
        63 -> Res.drawable._63
        64 -> Res.drawable._64
        65 -> Res.drawable._65
        66 -> Res.drawable._66
        67 -> Res.drawable._67
        68 -> Res.drawable._68
        69 -> Res.drawable._69
        70 -> Res.drawable._70
        71 -> Res.drawable._71
        73 -> Res.drawable._73
        75 -> Res.drawable._75
        76 -> Res.drawable._76
        77 -> Res.drawable._77
        78 -> Res.drawable._78
        79 -> Res.drawable._79
        80 -> Res.drawable._80
        81 -> Res.drawable._81
        82 -> Res.drawable._82
        83 -> Res.drawable._83
        84 -> Res.drawable._84
        85 -> Res.drawable._85
        86 -> Res.drawable._86
        else -> Res.drawable._1
    }
}
