package com.chvma.wordfight.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chvma.wordfight.model.WordContent
import com.chvma.wordfight.storage.createWordStorage
import org.jetbrains.compose.resources.painterResource
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
import wordfight.composeapp.generated.resources._25
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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
private fun wordPainter(imageKey: Int): Painter = painterResource(
    when (imageKey) {
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
        25 -> Res.drawable._25
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
)

@Composable
fun WordItem(
    word: WordContent,
    onAdd: () -> Unit,
    isSaved: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = wordPainter(word.imageKey),
            contentDescription = word.word,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(60.dp),
        )
        
        Spacer(Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = word.word,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = word.translation,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
            )
        }
        
        Box(
            modifier = Modifier
                .clickable { onAdd() }
                .padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (isSaved) "✓" else "+",
                color = if (isSaved) Color(0xFF4CAF50) else Color(0xFF2196F3),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun GameOverScreen(
    score: Int,
    bestScore: Int,
    missedWords: List<WordContent>,
    onRestart: () -> Unit,
    onHome: () -> Unit = {},
) {
    val wordStorage = remember { createWordStorage() }
    var savedWords by remember { mutableStateOf<Set<String>>(emptySet()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(missedWords) {
        savedWords = withContext(Dispatchers.Default) {
            missedWords.mapNotNull { word ->
                if (wordStorage.isWordSaved(word.word)) word.word else null
            }.toSet()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
            .safeContentPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Game Over",
            color = Color.White,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Score: $score",
            color = Color(0xFFFFD700),
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Best: $bestScore",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 20.sp,
        )

        if (missedWords.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))

            Text(
                text = "Missed Words:",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
            ) {
                items(missedWords) { word ->
                    WordItem(
                        word = word,
                        onAdd = {
                            if (!savedWords.contains(word.word)) {
                                scope.launch(Dispatchers.Default) {
                                    wordStorage.saveWord(word)
                                    savedWords = savedWords + word.word
                                }
                            }
                        },
                        isSaved = savedWords.contains(word.word),
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Button(
                onClick = onHome,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575)),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "Home",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "Play Again",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}
