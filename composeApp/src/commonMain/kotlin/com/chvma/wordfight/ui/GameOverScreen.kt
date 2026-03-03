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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chvma.wordfight.ads.BannerAdView
import com.chvma.wordfight.localization.AppLanguage
import com.chvma.wordfight.localization.AppStrings
import com.chvma.wordfight.model.WordContent
import com.chvma.wordfight.speech.createSpeechPlayer
import com.chvma.wordfight.storage.createWordStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource

@Composable
fun WordItem(
    word: WordContent,
    onAdd: () -> Unit,
    isSaved: Boolean = false,
    onSpeak: (String) -> Unit,
    language: AppLanguage,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clickable { onSpeak(word.word) },
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(word.res),
                contentDescription = word.word,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
            Text(
                text = "🔊",
                modifier = Modifier.padding(bottom = 3.dp, start = 3.dp).alpha(0.5f),
                fontSize = 22.sp,
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = word.word,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = word.translationFor(language),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
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
                color = if (isSaved) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameOverScreen(
    score: Int,
    bestScore: Int,
    missedWords: List<WordContent>,
    onRestart: () -> Unit,
    onHome: () -> Unit = {},
    musicEnabled: Boolean,
    onToggleMusic: () -> Unit,
    language: AppLanguage,
    strings: AppStrings,
) {
    val speechPlayer = remember { createSpeechPlayer() }
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

    DisposableEffect(Unit) {
        onDispose { speechPlayer.stop() }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.gameOver,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    MusicToggleButton(
                        isEnabled = musicEnabled,
                        onToggle = onToggleMusic,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            BannerAdView()
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = strings.gameOver + "!",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "${strings.scoreLabel}: $score",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "${strings.bestLabel}: $bestScore",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                )

                if (missedWords.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "${strings.missedWords}:",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp),
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
                                onSpeak = { text -> speechPlayer.speak(text) },
                                language = language,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = onHome,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    ) {
                        Text(
                            text = strings.home,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    Button(
                        onClick = onRestart,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    ) {
                        Text(
                            text = strings.playAgain,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }
    }
}
