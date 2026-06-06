package com.chvma.wordfight.content

import com.chvma.wordfight.model.Translation
import com.chvma.wordfight.model.WordCategory
import com.chvma.wordfight.model.WordContent
import org.jetbrains.compose.resources.DrawableResource
import wordfight.composeapp.generated.resources.Res
import wordfight.composeapp.generated.resources.*

/**
 * Catalogue of playable words. Each entry pairs an English word with its
 * translations, a difficulty [WordContent.level] (1 = easy/short/frequent,
 * 3 = long/rare) and the 3D illustration that depicts it.
 *
 * `id` is a stable integer used as the persistence key for saved words
 * ([com.chvma.wordfight.storage.WordStorageImpl]); keep existing ids stable.
 */
object WordRepository {
    private val rawWords: List<WordContent> = listOf(
        // --- Animals ---
        word(1, "cat", "кошка", "кіт", "gato", "chat", 1, Res.drawable.cat_3d),
        word(2, "dog", "собака", "собака", "perro", "chien", 1, Res.drawable.dog_3d),
        word(3, "bird", "птица", "птах", "pájaro", "oiseau", 1, Res.drawable.bird_3d),
        word(4, "fish", "рыба", "риба", "pez", "poisson", 1, Res.drawable.fish_3d),
        word(5, "horse", "лошадь", "кінь", "caballo", "cheval", 1, Res.drawable.horse_3d),
        word(6, "cow", "корова", "корова", "vaca", "vache", 1, Res.drawable.cow_3d),
        word(7, "pig", "свинья", "свиня", "cerdo", "cochon", 1, Res.drawable.pig_3d),
        word(8, "duck", "утка", "качка", "pato", "canard", 1, Res.drawable.duck_3d),
        word(9, "chicken", "курица", "курка", "pollo", "poulet", 2, Res.drawable.chicken_3d),
        word(10, "rooster", "петух", "півень", "gallo", "coq", 2, Res.drawable.rooster_3d),
        word(11, "goat", "коза", "коза", "cabra", "chèvre", 1, Res.drawable.goat_3d),
        word(12, "ram", "баран", "баран", "carnero", "bélier", 2, Res.drawable.ram_3d),
        word(13, "ox", "бык", "віл", "buey", "bœuf", 1, Res.drawable.ox_3d),
        word(14, "donkey", "осёл", "віслюк", "burro", "âne", 2, Res.drawable.donkey_3d),
        word(15, "rabbit", "кролик", "кролик", "conejo", "lapin", 2, Res.drawable.rabbit_3d),
        word(16, "mouse", "мышь", "миша", "ratón", "souris", 1, Res.drawable.mouse_3d),
        word(17, "rat", "крыса", "щур", "rata", "rat", 1, Res.drawable.rat_3d),
        word(18, "hamster", "хомяк", "хом'як", "hámster", "hamster", 2, Res.drawable.hamster_3d),
        word(19, "hedgehog", "ёж", "їжак", "erizo", "hérisson", 2, Res.drawable.hedgehog_3d),
        word(20, "fox", "лиса", "лисиця", "zorro", "renard", 1, Res.drawable.fox_3d),
        word(21, "wolf", "волк", "вовк", "lobo", "loup", 1, Res.drawable.wolf_3d),
        word(22, "bear", "медведь", "ведмідь", "oso", "ours", 1, Res.drawable.bear_3d),
        word(23, "polar bear", "белый медведь", "білий ведмідь", "oso polar", "ours polaire", 3, Res.drawable.polar_bear_3d),
        word(24, "panda", "панда", "панда", "panda", "panda", 1, Res.drawable.panda_3d),
        word(25, "koala", "коала", "коала", "koala", "koala", 2, Res.drawable.koala_3d),
        word(26, "lion", "лев", "лев", "león", "lion", 1, Res.drawable.lion_3d),
        word(27, "tiger", "тигр", "тигр", "tigre", "tigre", 1, Res.drawable.tiger_3d),
        word(28, "leopard", "леопард", "леопард", "leopardo", "léopard", 2, Res.drawable.leopard_3d),
        word(29, "elephant", "слон", "слон", "elefante", "éléphant", 2, Res.drawable.elephant_3d),
        word(30, "giraffe", "жираф", "жираф", "jirafa", "girafe", 2, Res.drawable.giraffe_3d),
        word(31, "zebra", "зебра", "зебра", "cebra", "zèbre", 2, Res.drawable.zebra_3d),
        word(32, "monkey", "обезьяна", "мавпа", "mono", "singe", 2, Res.drawable.monkey_3d),
        word(33, "gorilla", "горилла", "горила", "gorila", "gorille", 2, Res.drawable.gorilla_3d),
        word(34, "orangutan", "орангутан", "орангутан", "orangután", "orang-outan", 3, Res.drawable.orangutan_3d),
        word(35, "camel", "верблюд", "верблюд", "camello", "chameau", 2, Res.drawable.camel_3d),
        word(36, "kangaroo", "кенгуру", "кенгуру", "canguro", "kangourou", 2, Res.drawable.kangaroo_3d),
        word(37, "deer", "олень", "олень", "ciervo", "cerf", 1, Res.drawable.deer_3d),
        word(38, "moose", "лось", "лось", "alce", "élan", 2, Res.drawable.moose_3d),
        word(39, "bison", "бизон", "бізон", "bisonte", "bison", 2, Res.drawable.bison_3d),
        word(40, "boar", "кабан", "кабан", "jabalí", "sanglier", 2, Res.drawable.boar_3d),
        word(41, "rhinoceros", "носорог", "носоріг", "rinoceronte", "rhinocéros", 3, Res.drawable.rhinoceros_3d),
        word(42, "hippopotamus", "бегемот", "бегемот", "hipopótamo", "hippopotame", 3, Res.drawable.hippopotamus_3d),
        word(43, "crocodile", "крокодил", "крокодил", "cocodrilo", "crocodile", 2, Res.drawable.crocodile_3d),
        word(44, "snake", "змея", "змія", "serpiente", "serpent", 1, Res.drawable.snake_3d),
        word(45, "lizard", "ящерица", "ящірка", "lagarto", "lézard", 2, Res.drawable.lizard_3d),
        word(46, "frog", "лягушка", "жаба", "rana", "grenouille", 1, Res.drawable.frog_3d),
        word(47, "turtle", "черепаха", "черепаха", "tortuga", "tortue", 2, Res.drawable.turtle_3d),
        word(48, "snail", "улитка", "равлик", "caracol", "escargot", 2, Res.drawable.snail_3d),
        word(49, "owl", "сова", "сова", "búho", "hibou", 1, Res.drawable.owl_3d),
        word(50, "eagle", "орёл", "орел", "águila", "aigle", 2, Res.drawable.eagle_3d),
        word(51, "dove", "голубь", "голуб", "paloma", "colombe", 1, Res.drawable.dove_3d),
        word(52, "swan", "лебедь", "лебідь", "cisne", "cygne", 1, Res.drawable.swan_3d),
        word(53, "flamingo", "фламинго", "фламінго", "flamenco", "flamant", 2, Res.drawable.flamingo_3d),
        word(54, "parrot", "попугай", "папуга", "loro", "perroquet", 2, Res.drawable.parrot_3d),
        word(55, "penguin", "пингвин", "пінгвін", "pingüino", "pingouin", 2, Res.drawable.penguin_3d),
        word(56, "turkey", "индейка", "індичка", "pavo", "dinde", 2, Res.drawable.turkey_3d),
        word(57, "whale", "кит", "кит", "ballena", "baleine", 1, Res.drawable.whale_3d),
        word(58, "dolphin", "дельфин", "дельфін", "delfín", "dauphin", 2, Res.drawable.dolphin_3d),
        word(59, "shark", "акула", "акула", "tiburón", "requin", 2, Res.drawable.shark_3d),
        word(60, "octopus", "осьминог", "восьминіг", "pulpo", "poulpe", 2, Res.drawable.octopus_3d),
        word(61, "crab", "краб", "краб", "cangrejo", "crabe", 1, Res.drawable.crab_3d),
        word(62, "lobster", "омар", "омар", "langosta", "homard", 2, Res.drawable.lobster_3d),
        word(63, "shrimp", "креветка", "креветка", "camarón", "crevette", 2, Res.drawable.shrimp_3d),
        word(64, "squid", "кальмар", "кальмар", "calamar", "calmar", 2, Res.drawable.squid_3d),
        word(65, "seal", "тюлень", "тюлень", "foca", "phoque", 1, Res.drawable.seal_3d),
        word(66, "otter", "выдра", "видра", "nutria", "loutre", 2, Res.drawable.otter_3d),
        word(67, "beaver", "бобр", "бобер", "castor", "castor", 2, Res.drawable.beaver_3d),
        word(68, "badger", "барсук", "борсук", "tejón", "blaireau", 2, Res.drawable.badger_3d),
        word(69, "raccoon", "енот", "єнот", "mapache", "raton laveur", 2, Res.drawable.raccoon_3d),
        word(70, "skunk", "скунс", "скунс", "mofeta", "mouffette", 2, Res.drawable.skunk_3d),
        word(71, "sloth", "ленивец", "лінивець", "perezoso", "paresseux", 2, Res.drawable.sloth_3d),
        word(72, "llama", "лама", "лама", "llama", "lama", 1, Res.drawable.llama_3d),
        word(73, "bat", "летучая мышь", "кажан", "murciélago", "chauve-souris", 2, Res.drawable.bat_3d),
        word(74, "ant", "муравей", "мураха", "hormiga", "fourmi", 1, Res.drawable.ant_3d),
        word(75, "bee", "пчела", "бджола", "abeja", "abeille", 1, Res.drawable.honeybee_3d),
        word(76, "butterfly", "бабочка", "метелик", "mariposa", "papillon", 2, Res.drawable.butterfly_3d),
        word(77, "spider", "паук", "павук", "araña", "araignée", 1, Res.drawable.spider_3d),
        word(78, "beetle", "жук", "жук", "escarabajo", "scarabée", 2, Res.drawable.beetle_3d),
        word(79, "ladybug", "божья коровка", "сонечко", "mariquita", "coccinelle", 3, Res.drawable.lady_beetle_3d),
        word(80, "mosquito", "комар", "комар", "mosquito", "moustique", 2, Res.drawable.mosquito_3d),
        word(81, "fly", "муха", "муха", "mosca", "mouche", 1, Res.drawable.fly_3d),
        word(82, "cockroach", "таракан", "тарган", "cucaracha", "cafard", 2, Res.drawable.cockroach_3d),
        word(83, "worm", "червяк", "черв'як", "gusano", "ver", 1, Res.drawable.worm_3d),
        word(84, "dragon", "дракон", "дракон", "dragón", "dragon", 1, Res.drawable.dragon_3d),
        word(85, "unicorn", "единорог", "єдиноріг", "unicornio", "licorne", 2, Res.drawable.unicorn_3d),

        // --- Fruit & vegetables ---
        word(86, "apple", "яблоко", "яблуко", "manzana", "pomme", 1, Res.drawable.red_apple_3d),
        word(87, "banana", "банан", "банан", "plátano", "banane", 1, Res.drawable.banana_3d),
        word(88, "pear", "груша", "груша", "pera", "poire", 1, Res.drawable.pear_3d),
        word(89, "peach", "персик", "персик", "melocotón", "pêche", 2, Res.drawable.peach_3d),
        word(90, "grapes", "виноград", "виноград", "uvas", "raisins", 2, Res.drawable.grapes_3d),
        word(91, "strawberry", "клубника", "полуниця", "fresa", "fraise", 2, Res.drawable.strawberry_3d),
        word(92, "blueberries", "черника", "чорниця", "arándanos", "myrtilles", 3, Res.drawable.blueberries_3d),
        word(93, "cherries", "вишня", "вишня", "cerezas", "cerises", 2, Res.drawable.cherries_3d),
        word(94, "watermelon", "арбуз", "кавун", "sandía", "pastèque", 2, Res.drawable.watermelon_3d),
        word(95, "melon", "дыня", "диня", "melón", "melon", 1, Res.drawable.melon_3d),
        word(96, "pineapple", "ананас", "ананас", "piña", "ananas", 2, Res.drawable.pineapple_3d),
        word(97, "coconut", "кокос", "кокос", "coco", "noix de coco", 2, Res.drawable.coconut_3d),
        word(98, "lemon", "лимон", "лимон", "limón", "citron", 1, Res.drawable.lemon_3d),
        word(99, "lime", "лайм", "лайм", "lima", "citron vert", 2, Res.drawable.lime_3d),
        word(100, "mango", "манго", "манго", "mango", "mangue", 1, Res.drawable.mango_3d),
        word(101, "kiwi", "киви", "ківі", "kiwi", "kiwi", 1, Res.drawable.kiwi_fruit_3d),
        word(102, "avocado", "авокадо", "авокадо", "aguacate", "avocat", 2, Res.drawable.avocado_3d),
        word(103, "olive", "оливка", "оливка", "aceituna", "olive", 2, Res.drawable.olive_3d),
        word(104, "tomato", "помидор", "помідор", "tomate", "tomate", 2, Res.drawable.tomato_3d),
        word(105, "potato", "картофель", "картопля", "patata", "pomme de terre", 2, Res.drawable.potato_3d),
        word(106, "carrot", "морковь", "морква", "zanahoria", "carotte", 2, Res.drawable.carrot_3d),
        word(107, "onion", "лук", "цибуля", "cebolla", "oignon", 1, Res.drawable.onion_3d),
        word(108, "garlic", "чеснок", "часник", "ajo", "ail", 2, Res.drawable.garlic_3d),
        word(109, "cucumber", "огурец", "огірок", "pepino", "concombre", 2, Res.drawable.cucumber_3d),
        word(110, "eggplant", "баклажан", "баклажан", "berenjena", "aubergine", 2, Res.drawable.eggplant_3d),
        word(111, "broccoli", "брокколи", "броколі", "brócoli", "brocoli", 2, Res.drawable.broccoli_3d),
        word(112, "mushroom", "гриб", "гриб", "seta", "champignon", 1, Res.drawable.mushroom_3d),
        word(113, "pepper", "перец", "перець", "pimiento", "poivron", 2, Res.drawable.bell_pepper_3d),
        word(114, "beans", "фасоль", "квасоля", "frijoles", "haricots", 2, Res.drawable.beans_3d),

        // --- Food & drink ---
        word(115, "bread", "хлеб", "хліб", "pan", "pain", 1, Res.drawable.bread_3d),
        word(116, "baguette", "багет", "багет", "baguette", "baguette", 2, Res.drawable.baguette_bread_3d),
        word(117, "bagel", "бейгл", "бейгл", "bagel", "bagel", 2, Res.drawable.bagel_3d),
        word(118, "croissant", "круассан", "круасан", "croissant", "croissant", 2, Res.drawable.croissant_3d),
        word(119, "egg", "яйцо", "яйце", "huevo", "œuf", 1, Res.drawable.egg_3d),
        word(120, "bacon", "бекон", "бекон", "tocino", "bacon", 2, Res.drawable.bacon_3d),
        word(121, "cheese", "сыр", "сир", "queso", "fromage", 1, Res.drawable.cheese_wedge_3d),
        word(122, "sandwich", "бутерброд", "бутерброд", "sándwich", "sandwich", 2, Res.drawable.sandwich_3d),
        word(123, "hamburger", "гамбургер", "гамбургер", "hamburguesa", "hamburger", 2, Res.drawable.hamburger_3d),
        word(124, "hot dog", "хот-дог", "хот-дог", "perrito caliente", "hot-dog", 2, Res.drawable.hot_dog_3d),
        word(125, "pizza", "пицца", "піца", "pizza", "pizza", 1, Res.drawable.pizza_3d),
        word(126, "taco", "тако", "тако", "taco", "taco", 1, Res.drawable.taco_3d),
        word(127, "burrito", "буррито", "буріто", "burrito", "burrito", 2, Res.drawable.burrito_3d),
        word(128, "french fries", "картофель фри", "картопля фрі", "patatas fritas", "frites", 3, Res.drawable.french_fries_3d),
        word(129, "spaghetti", "спагетти", "спагеті", "espaguetis", "spaghetti", 2, Res.drawable.spaghetti_3d),
        word(130, "rice", "рис", "рис", "arroz", "riz", 1, Res.drawable.cooked_rice_3d),
        word(131, "sushi", "суши", "суші", "sushi", "sushi", 1, Res.drawable.sushi_3d),
        word(132, "pancakes", "блины", "млинці", "panqueques", "crêpes", 2, Res.drawable.pancakes_3d),
        word(133, "waffle", "вафля", "вафля", "gofre", "gaufre", 2, Res.drawable.waffle_3d),
        word(134, "cookie", "печенье", "печиво", "galleta", "biscuit", 2, Res.drawable.cookie_3d),
        word(135, "doughnut", "пончик", "пончик", "dona", "beignet", 2, Res.drawable.doughnut_3d),
        word(136, "cupcake", "кекс", "кекс", "magdalena", "cupcake", 2, Res.drawable.cupcake_3d),
        word(137, "cake", "торт", "торт", "pastel", "gâteau", 1, Res.drawable.birthday_cake_3d),
        word(138, "pie", "пирог", "пиріг", "tarta", "tarte", 1, Res.drawable.pie_3d),
        word(139, "candy", "конфета", "цукерка", "caramelo", "bonbon", 2, Res.drawable.candy_3d),
        word(140, "chocolate", "шоколад", "шоколад", "chocolate", "chocolat", 2, Res.drawable.chocolate_bar_3d),
        word(141, "ice cream", "мороженое", "морозиво", "helado", "glace", 2, Res.drawable.ice_cream_3d),
        word(142, "milk", "молоко", "молоко", "leche", "lait", 1, Res.drawable.glass_of_milk_3d),
        word(143, "coffee", "кофе", "кава", "café", "café", 1, Res.drawable.hot_beverage_3d),

        // --- Transport ---
        word(144, "car", "машина", "машина", "coche", "voiture", 1, Res.drawable.automobile_3d),
        word(145, "bus", "автобус", "автобус", "autobús", "bus", 1, Res.drawable.bus_3d),
        word(146, "tram", "трамвай", "трамвай", "tranvía", "tramway", 2, Res.drawable.tram_3d),
        word(147, "trolleybus", "троллейбус", "тролейбус", "trolebús", "trolleybus", 3, Res.drawable.trolleybus_3d),
        word(148, "taxi", "такси", "таксі", "taxi", "taxi", 1, Res.drawable.taxi_3d),
        word(149, "police car", "полицейская машина", "поліцейська машина", "coche de policía", "voiture de police", 3, Res.drawable.police_car_3d),
        word(150, "ambulance", "скорая помощь", "швидка допомога", "ambulancia", "ambulance", 3, Res.drawable.ambulance_3d),
        word(151, "fire truck", "пожарная машина", "пожежна машина", "camión de bomberos", "camion de pompiers", 3, Res.drawable.fire_engine_3d),
        word(152, "truck", "грузовик", "вантажівка", "camión", "camion", 2, Res.drawable.delivery_truck_3d),
        word(153, "tractor", "трактор", "трактор", "tractor", "tracteur", 2, Res.drawable.tractor_3d),
        word(154, "motorcycle", "мотоцикл", "мотоцикл", "motocicleta", "moto", 2, Res.drawable.motorcycle_3d),
        word(155, "scooter", "самокат", "самокат", "patinete", "trottinette", 2, Res.drawable.kick_scooter_3d),
        word(156, "bicycle", "велосипед", "велосипед", "bicicleta", "vélo", 2, Res.drawable.bicycle_3d),
        word(157, "skateboard", "скейтборд", "скейтборд", "monopatín", "skateboard", 2, Res.drawable.skateboard_3d),
        word(158, "train", "поезд", "потяг", "tren", "train", 1, Res.drawable.train_3d),
        word(159, "airplane", "самолёт", "літак", "avión", "avion", 2, Res.drawable.airplane_3d),
        word(160, "helicopter", "вертолёт", "гелікоптер", "helicóptero", "hélicoptère", 3, Res.drawable.helicopter_3d),
        word(161, "rocket", "ракета", "ракета", "cohete", "fusée", 2, Res.drawable.rocket_3d),
        word(162, "ship", "корабль", "корабель", "barco", "navire", 1, Res.drawable.ship_3d),
        word(163, "ferry", "паром", "пором", "ferry", "ferry", 2, Res.drawable.ferry_3d),
        word(164, "boat", "лодка", "човен", "lancha", "bateau", 1, Res.drawable.motor_boat_3d),
        word(165, "canoe", "каноэ", "каное", "canoa", "canoë", 2, Res.drawable.canoe_3d),
        word(166, "lifebuoy", "спасательный круг", "рятувальний круг", "salvavidas", "bouée", 3, Res.drawable.ring_buoy_3d),

        // --- Home & objects ---
        word(167, "bed", "кровать", "ліжко", "cama", "lit", 1, Res.drawable.bed_3d),
        word(168, "chair", "стул", "стілець", "silla", "chaise", 1, Res.drawable.chair_3d),
        word(169, "sofa", "диван", "диван", "sofá", "canapé", 1, Res.drawable.couch_and_lamp_3d),
        word(170, "door", "дверь", "двері", "puerta", "porte", 1, Res.drawable.door_3d),
        word(171, "window", "окно", "вікно", "ventana", "fenêtre", 1, Res.drawable.window_3d),
        word(172, "mirror", "зеркало", "дзеркало", "espejo", "miroir", 1, Res.drawable.mirror_3d),
        word(173, "bathtub", "ванна", "ванна", "bañera", "baignoire", 2, Res.drawable.bathtub_3d),
        word(174, "shower", "душ", "душ", "ducha", "douche", 1, Res.drawable.shower_3d),
        word(175, "toilet", "туалет", "туалет", "inodoro", "toilettes", 2, Res.drawable.toilet_3d),
        word(176, "candle", "свеча", "свічка", "vela", "bougie", 2, Res.drawable.candle_3d),
        word(177, "light bulb", "лампочка", "лампочка", "bombilla", "ampoule", 2, Res.drawable.light_bulb_3d),
        word(178, "plug", "вилка", "вилка", "enchufe", "prise", 2, Res.drawable.electric_plug_3d),
        word(179, "battery", "батарейка", "батарейка", "pila", "pile", 2, Res.drawable.battery_3d),
        word(180, "key", "ключ", "ключ", "llave", "clé", 1, Res.drawable.key_3d),
        word(181, "broom", "метла", "мітла", "escoba", "balai", 2, Res.drawable.broom_3d),
        word(182, "bucket", "ведро", "відро", "cubo", "seau", 2, Res.drawable.bucket_3d),
        word(183, "sponge", "губка", "губка", "esponja", "éponge", 2, Res.drawable.sponge_3d),
        word(184, "soap", "мыло", "мило", "jabón", "savon", 1, Res.drawable.soap_3d),
        word(185, "toothbrush", "зубная щётка", "зубна щітка", "cepillo de dientes", "brosse à dents", 3, Res.drawable.toothbrush_3d),
        word(186, "ladder", "лестница", "драбина", "escalera", "échelle", 2, Res.drawable.ladder_3d),
        word(187, "magnet", "магнит", "магніт", "imán", "aimant", 2, Res.drawable.magnet_3d),
        word(188, "gear", "шестерёнка", "шестерня", "engranaje", "engrenage", 3, Res.drawable.gear_3d),
        word(189, "barrel", "бочка", "бочка", "barril", "baril", 2, Res.drawable.oil_drum_3d),
        word(190, "basket", "корзина", "кошик", "cesta", "panier", 1, Res.drawable.basket_3d),
        word(191, "package", "посылка", "посилка", "paquete", "colis", 2, Res.drawable.package_3d),
        word(192, "gift", "подарок", "подарунок", "regalo", "cadeau", 1, Res.drawable.wrapped_gift_3d),
        word(193, "crown", "корона", "корона", "corona", "couronne", 1, Res.drawable.crown_3d),
        word(194, "ring", "кольцо", "каблучка", "anillo", "bague", 1, Res.drawable.ring_3d),

        // --- Tools ---
        word(195, "hammer", "молоток", "молоток", "martillo", "marteau", 2, Res.drawable.hammer_3d),
        word(196, "wrench", "гаечный ключ", "гайковий ключ", "llave inglesa", "clé à molette", 3, Res.drawable.wrench_3d),
        word(197, "screwdriver", "отвёртка", "викрутка", "destornillador", "tournevis", 3, Res.drawable.screwdriver_3d),
        word(198, "axe", "топор", "сокира", "hacha", "hache", 1, Res.drawable.axe_3d),
        word(199, "toolbox", "ящик с инструментами", "ящик з інструментами", "caja de herramientas", "boîte à outils", 3, Res.drawable.toolbox_3d),
        word(200, "paintbrush", "кисть", "пензель", "pincel", "pinceau", 2, Res.drawable.paintbrush_3d),
        word(201, "scissors", "ножницы", "ножиці", "tijeras", "ciseaux", 2, Res.drawable.scissors_3d),

        // --- School & office ---
        word(202, "pen", "ручка", "ручка", "bolígrafo", "stylo", 1, Res.drawable.pen_3d),
        word(203, "pencil", "карандаш", "олівець", "lápiz", "crayon", 2, Res.drawable.pencil_3d),
        word(204, "crayon", "мелок", "крейда", "crayón", "craie", 2, Res.drawable.crayon_3d),
        word(205, "notebook", "тетрадь", "зошит", "cuaderno", "cahier", 2, Res.drawable.notebook_3d),
        word(206, "bookmark", "закладка", "закладка", "marcador", "marque-page", 2, Res.drawable.bookmark_3d),
        word(207, "ruler", "линейка", "лінійка", "regla", "règle", 2, Res.drawable.straight_ruler_3d),
        word(208, "briefcase", "портфель", "портфель", "maletín", "mallette", 2, Res.drawable.briefcase_3d),
        word(209, "backpack", "рюкзак", "рюкзак", "mochila", "sac à dos", 2, Res.drawable.backpack_3d),
        word(210, "handbag", "сумка", "сумка", "bolso", "sac à main", 2, Res.drawable.handbag_3d),

        // --- Electronics ---
        word(211, "television", "телевизор", "телевізор", "televisión", "télévision", 2, Res.drawable.television_3d),
        word(212, "radio", "радио", "радіо", "radio", "radio", 1, Res.drawable.radio_3d),
        word(213, "phone", "телефон", "телефон", "teléfono", "téléphone", 2, Res.drawable.telephone_3d),
        word(214, "camera", "фотоаппарат", "фотоапарат", "cámara", "appareil photo", 2, Res.drawable.camera_3d),
        word(215, "keyboard", "клавиатура", "клавіатура", "teclado", "clavier", 2, Res.drawable.keyboard_3d),
        word(216, "computer", "компьютер", "комп'ютер", "ordenador", "ordinateur", 3, Res.drawable.desktop_computer_3d),
        word(217, "printer", "принтер", "принтер", "impresora", "imprimante", 2, Res.drawable.printer_3d),
        word(218, "headphones", "наушники", "навушники", "auriculares", "écouteurs", 2, Res.drawable.headphone_3d),
        word(219, "microphone", "микрофон", "мікрофон", "micrófono", "microphone", 2, Res.drawable.microphone_3d),
        word(220, "joystick", "джойстик", "джойстик", "joystick", "manette", 2, Res.drawable.joystick_3d),
        word(221, "telescope", "телескоп", "телескоп", "telescopio", "télescope", 2, Res.drawable.telescope_3d),
        word(222, "microscope", "микроскоп", "мікроскоп", "microscopio", "microscope", 3, Res.drawable.microscope_3d),

        // --- Musical instruments ---
        word(223, "guitar", "гитара", "гітара", "guitarra", "guitare", 2, Res.drawable.guitar_3d),
        word(224, "violin", "скрипка", "скрипка", "violín", "violon", 2, Res.drawable.violin_3d),
        word(225, "drum", "барабан", "барабан", "tambor", "tambour", 1, Res.drawable.drum_3d),
        word(226, "trumpet", "труба", "труба", "trompeta", "trompette", 2, Res.drawable.trumpet_3d),
        word(227, "saxophone", "саксофон", "саксофон", "saxofón", "saxophone", 3, Res.drawable.saxophone_3d),
        word(228, "flute", "флейта", "флейта", "flauta", "flûte", 2, Res.drawable.flute_3d),
        word(229, "piano", "пианино", "піаніно", "piano", "piano", 2, Res.drawable.musical_keyboard_3d),

        // --- Clothing ---
        word(230, "dress", "платье", "сукня", "vestido", "robe", 1, Res.drawable.dress_3d),
        word(231, "coat", "пальто", "пальто", "abrigo", "manteau", 1, Res.drawable.coat_3d),
        word(232, "jeans", "джинсы", "джинси", "vaqueros", "jean", 2, Res.drawable.jeans_3d),
        word(233, "shorts", "шорты", "шорти", "pantalones cortos", "short", 2, Res.drawable.shorts_3d),
        word(234, "socks", "носки", "шкарпетки", "calcetines", "chaussettes", 2, Res.drawable.socks_3d),
        word(235, "gloves", "перчатки", "рукавички", "guantes", "gants", 2, Res.drawable.gloves_3d),
        word(236, "scarf", "шарф", "шарф", "bufanda", "écharpe", 1, Res.drawable.scarf_3d),
        word(237, "tie", "галстук", "краватка", "corbata", "cravate", 2, Res.drawable.necktie_3d),
        word(238, "cap", "кепка", "кепка", "gorra", "casquette", 2, Res.drawable.billed_cap_3d),
        word(239, "shoe", "туфля", "черевик", "zapato", "chaussure", 1, Res.drawable.mans_shoe_3d),
        word(240, "boot", "ботинок", "чобіт", "bota", "botte", 1, Res.drawable.hiking_boot_3d),
        word(241, "bikini", "бикини", "бікіні", "bikini", "bikini", 2, Res.drawable.bikini_3d),
        word(242, "lipstick", "помада", "помада", "pintalabios", "rouge à lèvres", 3, Res.drawable.lipstick_3d),

        // --- Sports ---
        word(243, "football", "футбол", "футбол", "fútbol", "football", 2, Res.drawable.soccer_ball_3d),
        word(244, "basketball", "баскетбол", "баскетбол", "baloncesto", "basket-ball", 2, Res.drawable.basketball_3d),
        word(245, "baseball", "бейсбол", "бейсбол", "béisbol", "baseball", 2, Res.drawable.baseball_3d),
        word(246, "volleyball", "волейбол", "волейбол", "voleibol", "volley-ball", 2, Res.drawable.volleyball_3d),
        word(247, "American football", "американский футбол", "американський футбол", "fútbol americano", "football américain", 3, Res.drawable.american_football_3d),
        word(248, "hockey", "хоккей", "хокей", "hockey", "hockey", 2, Res.drawable.ice_hockey_3d),
        word(249, "tennis", "теннис", "теніс", "tenis", "tennis", 2, Res.drawable.tennis_3d),
        word(250, "badminton", "бадминтон", "бадмінтон", "bádminton", "badminton", 3, Res.drawable.badminton_3d),
        word(251, "boxing", "бокс", "бокс", "boxeo", "boxe", 2, Res.drawable.boxing_glove_3d),
        word(252, "skis", "лыжи", "лижі", "esquís", "skis", 2, Res.drawable.skis_3d),

        // --- Nature & misc ---
        word(253, "cloud", "облако", "хмара", "nube", "nuage", 1, Res.drawable.cloud_3d),
        word(254, "rainbow", "радуга", "веселка", "arcoíris", "arc-en-ciel", 2, Res.drawable.rainbow_3d),
        word(255, "fire", "огонь", "вогонь", "fuego", "feu", 1, Res.drawable.fire_3d),
        word(256, "drop", "капля", "крапля", "gota", "goutte", 1, Res.drawable.droplet_3d),
        word(257, "mountain", "гора", "гора", "montaña", "montagne", 1, Res.drawable.mountain_3d),
        word(258, "desert", "пустыня", "пустеля", "desierto", "désert", 2, Res.drawable.desert_3d),
        word(259, "cactus", "кактус", "кактус", "cactus", "cactus", 2, Res.drawable.cactus_3d),
        word(260, "rose", "роза", "троянда", "rosa", "rose", 1, Res.drawable.rose_3d),
        word(261, "bouquet", "букет", "букет", "ramo", "bouquet", 2, Res.drawable.bouquet_3d),
        word(262, "lotus", "лотос", "лотос", "loto", "lotus", 2, Res.drawable.lotus_3d),
        word(263, "clover", "клевер", "конюшина", "trébol", "trèfle", 2, Res.drawable.shamrock_3d),
        word(264, "herb", "травы", "трави", "hierba", "herbe", 2, Res.drawable.herb_3d),
        word(265, "balloon", "воздушный шар", "повітряна кулька", "globo", "ballon", 2, Res.drawable.balloon_3d),
        word(266, "kite", "воздушный змей", "повітряний змій", "cometa", "cerf-volant", 3, Res.drawable.kite_3d),
        word(267, "teddy bear", "плюшевый мишка", "плюшевий ведмедик", "oso de peluche", "ours en peluche", 3, Res.drawable.teddy_bear_3d),
        word(268, "puzzle", "пазл", "пазл", "rompecabezas", "puzzle", 2, Res.drawable.puzzle_piece_3d),
        word(269, "dice", "кубик", "кубик", "dado", "dé", 2, Res.drawable.game_die_3d),
        word(270, "coin", "монета", "монета", "moneda", "pièce", 1, Res.drawable.coin_3d),
        word(271, "watch", "часы", "годинник", "reloj", "montre", 1, Res.drawable.watch_3d),
        word(272, "alarm clock", "будильник", "будильник", "despertador", "réveil", 2, Res.drawable.alarm_clock_3d),
        word(273, "stopwatch", "секундомер", "секундомір", "cronómetro", "chronomètre", 3, Res.drawable.stopwatch_3d),
    )

    /**
     * Maps a word [id] to its [WordCategory]. The catalogue above is laid out in
     * contiguous, category-ordered id blocks, so the category is derived from the
     * id range rather than repeated on all 270+ entries. Keep these ranges in sync
     * with the section comments above when adding words.
     */
    private fun categoryFor(id: Int): WordCategory = when (id) {
        in 1..85 -> WordCategory.ANIMALS
        in 86..114 -> WordCategory.FRUIT_VEG
        in 115..143 -> WordCategory.FOOD
        in 144..166 -> WordCategory.TRANSPORT
        in 167..194 -> WordCategory.HOME
        in 195..201 -> WordCategory.TOOLS
        in 202..210 -> WordCategory.SCHOOL
        in 211..222 -> WordCategory.ELECTRONICS
        in 223..229 -> WordCategory.MUSIC
        in 230..242 -> WordCategory.CLOTHING
        in 243..252 -> WordCategory.SPORTS
        in 253..273 -> WordCategory.NATURE
        else -> WordCategory.OTHER
    }

    val words: List<WordContent> = rawWords.map { it.copy(category = categoryFor(it.id)) }

    private val wordsById: Map<Int, WordContent> = words.associateBy { it.id }
    private val wordsByWord: Map<String, WordContent> = words.associateBy { it.word }

    fun byId(id: Int): WordContent? = wordsById[id]

    fun byWord(word: String): WordContent? = wordsByWord[word]

    /**
     * Words to spawn for a focused session. [category] = null means all topics,
     * [level] = null means all difficulties (1 = easy … 3 = hard).
     */
    fun wordsFor(category: WordCategory?, level: Int?): List<WordContent> =
        words.filter { word ->
            (category == null || word.category == category) &&
                (level == null || word.level == level)
        }

    /** How many words exist for the given [category]/[level] filter. */
    fun countFor(category: WordCategory?, level: Int?): Int = wordsFor(category, level).size

    private fun word(
        id: Int,
        text: String,
        ru: String,
        ua: String,
        es: String,
        fr: String,
        level: Int,
        res: DrawableResource,
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
            res = res,
        )
    }
}
