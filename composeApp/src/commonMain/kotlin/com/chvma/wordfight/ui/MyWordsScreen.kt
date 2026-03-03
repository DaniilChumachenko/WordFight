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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyWordsScreen(
    onBack: () -> Unit,
    musicEnabled: Boolean,
    onToggleMusic: () -> Unit,
    language: AppLanguage,
    strings: AppStrings,
) {
    val speechPlayer = remember { createSpeechPlayer() }
    val wordStorage = remember { createWordStorage() }
    var words by remember { mutableStateOf<List<WordContent>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        words = withContext(Dispatchers.Default) {
            wordStorage.getAllWords()
        }
    }

    DisposableEffect(Unit) {
        onDispose { speechPlayer.stop() }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        BackButtonContent()
                    }
                },
                title = {
                    Text(
                        text = strings.myWords,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp,
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
        if (words.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = strings.noSavedWords,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontSize = 18.sp,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                items(words) { word ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clickable { speechPlayer.speak(word.word) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                painter = painterResource(word.res),
                                contentDescription = word.word,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize(),
                            )
                            Text(
                                modifier = Modifier.padding(bottom = 3.dp, start = 3.dp).alpha(0.5f),
                                text = "🔊",
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
                                .clickable {
                                    scope.launch(Dispatchers.Default) {
                                        wordStorage.deleteWord(word.word)
                                        words = words.filter { it.word != word.word }
                                    }
                                }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "×",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}
