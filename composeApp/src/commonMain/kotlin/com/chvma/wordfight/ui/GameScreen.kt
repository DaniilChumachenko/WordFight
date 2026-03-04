package com.chvma.wordfight.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chvma.wordfight.ads.BannerAdView
import com.chvma.wordfight.engine.GameEngine
import com.chvma.wordfight.haptics.HapticType
import com.chvma.wordfight.haptics.createHapticEngine
import com.chvma.wordfight.localization.AppLanguage
import com.chvma.wordfight.localization.AppStrings
import com.chvma.wordfight.speech.SpeechEngine
import com.chvma.wordfight.speech.createSpeechPlayer
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import wordfight.composeapp.generated.resources.Res
import kotlin.math.PI
import kotlin.math.sin
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

private const val NIGHT_SKY_JSON_PATH = "files/moon_night_sky.json"
private const val DAY_SKY_JSON_PATH = "files/day_sky.json"

private var cachedNightSkyJson: String? = null
private var cachedDaySkyJson: String? = null

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    gameEngine: GameEngine,
    speechEngine: SpeechEngine,
    onGameOver: (score: Int, best: Int) -> Unit,
    onBack: () -> Unit = {},
    musicEnabled: Boolean,
    onToggleMusic: () -> Unit,
    language: AppLanguage,
    strings: AppStrings,
    onPauseWithAd: (onResult: (rewarded: Boolean) -> Unit) -> Unit,
    onReviveWithAd: (onResult: (rewarded: Boolean) -> Unit) -> Unit,
) {
    val state by gameEngine.state.collectAsState()
    val haptic = remember { createHapticEngine() }
    val speechPlayer = remember { createSpeechPlayer() }
    val composeHaptic = LocalHapticFeedback.current
    var previousLives by remember { mutableIntStateOf(state.lives) }
    var missedWord by remember { mutableStateOf(state.lastMissedWord) }
    var showMissed by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    var damageActive by remember { mutableStateOf(false) }
    var damageToken by remember { mutableIntStateOf(0) }
    var showReviveDialog by remember { mutableStateOf(false) }
    var reviveInProgress by remember { mutableStateOf(false) }
    var gameOverHandled by remember { mutableStateOf(false) }
    var autoPausedByLifecycle by remember { mutableStateOf(false) }
    var appWasBackgrounded by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestState by rememberUpdatedState(state)

    var screenWidth by remember { mutableFloatStateOf(0f) }
    var screenHeight by remember { mutableFloatStateOf(0f) }

    // Game loop
    LaunchedEffect(Unit) {
        var lastTime = 0L
        while (isActive) {
            val time = withFrameMillis { it }
            val delta = if (lastTime == 0L) 0.016f else ((time - lastTime) / 1000f).coerceAtMost(0.1f)
            lastTime = time
            gameEngine.update(delta, screenWidth, screenHeight)
        }
    }

    // Speech recognition
    LaunchedEffect(Unit) {
        speechEngine.start("en-US")
        speechEngine.partialFlow.collect { spoken ->
            gameEngine.tryMatch(spoken)
        }
    }
    LaunchedEffect(Unit) {
        speechEngine.processingFlow.collect { processing ->
            if (processing) {
                gameEngine.setProcessingSlowdown(true)
                gameEngine.setSpeedScales(fall = 1f, spawn = 0.8f)
            } else {
                gameEngine.setProcessingSlowdown(false)
                gameEngine.setSpeedScales(fall = 1f, spawn = 1f)
            }
            isSpeaking = processing
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechPlayer.stop()
            speechEngine.stop()
            gameEngine.setProcessingSlowdown(false)
            gameEngine.setSpeedScales(fall = 1f, spawn = 1f)
        }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    appWasBackgrounded = true
                    speechEngine.stop()
                    val s = latestState
                    if (!s.isGameOver && !s.isPaused) {
                        gameEngine.pause()
                        autoPausedByLifecycle = true
                    } else {
                        autoPausedByLifecycle = false
                    }
                }
                Lifecycle.Event.ON_START -> {
                    if (appWasBackgrounded) {
                        val s = latestState
                        if (!s.isGameOver) {
                            speechEngine.start("en-US")
                        }
                        if (autoPausedByLifecycle && !s.isGameOver) {
                            gameEngine.resume()
                        }
                        autoPausedByLifecycle = false
                        appWasBackgrounded = false
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    fun finishGame() {
        if (gameOverHandled) return
        gameOverHandled = true
        showReviveDialog = false
        onGameOver(state.score, state.bestScore)
    }

    // Game over with revive offer
    LaunchedEffect(
        state.isGameOver,
        state.lives,
        state.revivesUsed,
        showReviveDialog,
        reviveInProgress,
        gameOverHandled,
    ) {
        if (state.isGameOver &&
            state.lives <= 0 &&
            !showReviveDialog &&
            !reviveInProgress &&
            !gameOverHandled
        ) {
            haptic.perform(HapticType.GameOver)
            if (state.revivesUsed < GameEngine.MAX_REVIVES_PER_GAME) {
                showReviveDialog = true
            } else {
                finishGame()
            }
        }
    }
    LaunchedEffect(state.isGameOver) {
        if (!state.isGameOver) {
            showReviveDialog = false
            reviveInProgress = false
            gameOverHandled = false
        }
    }
    LaunchedEffect(state.lives) {
        val lost = previousLives - state.lives
        if (lost > 0) {
            composeHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
            repeat(lost) {
                haptic.perform(HapticType.LifeLost)
                delay(80)
            }
            damageActive = true
            damageToken += 1
        }
        previousLives = state.lives
    }
    LaunchedEffect(damageToken) {
        if (damageToken == 0) return@LaunchedEffect
        val token = damageToken
        delay(2500)
        if (damageToken == token) {
            damageActive = false
        }
    }
    LaunchedEffect(state.lastMissedToken) {
        val word = state.lastMissedWord ?: return@LaunchedEffect
        missedWord = word
        showMissed = true
        speechPlayer.speak(word.word)
        delay(2000)
        showMissed = false
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "❤️".repeat(state.lives.coerceIn(0, 5)),
                            fontSize = 18.sp,
                        )
                        Text(
                            text = "${strings.scoreLabel}: ${state.score}",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }
                },
                actions = {
                    // Pause/Resume
                    Text(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp),
                            )
                            .padding(vertical = 6.dp, horizontal = 10.dp)
                            .clickable {
                                if (state.isPaused) {
                                    gameEngine.resume()
                                } else if (gameEngine.canUsePause()) {
                                    onPauseWithAd { rewarded ->
                                        if (rewarded) {
                                            gameEngine.pauseWithLimit()
                                        }
                                    }
                                }
                            },
                        text = if (state.isPaused) "▶" else "⏸",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp,
                    )
                    Spacer(Modifier.width(6.dp))
                    // Exit
                    Text(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp),
                            )
                            .padding(vertical = 6.dp, horizontal = 10.dp)
                            .clickable {
                                speechEngine.stop()
                                onGameOver(state.score, state.bestScore)
                            },
                        text = "✕",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.width(4.dp))
                    // Music
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
                .onSizeChanged {
                    screenWidth = it.width.toFloat()
                    screenHeight = it.height.toFloat()
                }
        ) {
            AnimatedSkyBackground(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.45f),
            )

            // Falling cards
            if (screenWidth > 0f && screenHeight > 0f) {
                state.activeCards.forEach { card ->
                    FallingCard(
                        card = card,
                        screenWidth = screenWidth,
                        screenHeight = screenHeight,
                    )
                }
            }

            AnimatedVisibility(
                visible = showMissed && missedWord != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 56.dp)
            ) {
                val gradientBorder = Brush.linearGradient(
                    colors = listOf(Color(0xFF7CFFB2), Color(0xFF63D3FF), Color(0xFFFF9AEF)),
                )
                Column(
                    modifier = Modifier
                        .border(2.dp, gradientBorder, RoundedCornerShape(14.dp))
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(14.dp),
                        )
                        .padding(horizontal = 22.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = missedWord?.word.orEmpty(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = missedWord?.translationFor(language).orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            AnimatedStatusBar(
                mode = when {
                    damageActive -> StatusBarMode.Damage
                    isSpeaking -> StatusBarMode.Speaking
                    else -> StatusBarMode.Idle
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(14.dp),
            )

            if (showReviveDialog) {
                AlertDialog(
                    onDismissRequest = {
                        if (!reviveInProgress) finishGame()
                    },
                    containerColor = AlertDialogDefaults.containerColor,
                    titleContentColor = AlertDialogDefaults.titleContentColor,
                    textContentColor = AlertDialogDefaults.textContentColor,
                    title = { Text(text = strings.reviveLifeTitle) },
                    text = { Text(text = strings.reviveLifeMessage) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (reviveInProgress) return@TextButton
                                reviveInProgress = true
                                showReviveDialog = false
                                onReviveWithAd { rewarded ->
                                    reviveInProgress = false
                                    if (rewarded) {
                                        if (!gameEngine.reviveOneLife()) {
                                            finishGame()
                                        }
                                    } else {
                                        finishGame()
                                    }
                                }
                            }
                        ) {
                            Text(strings.watchVideo)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                if (!reviveInProgress) finishGame()
                            }
                        ) {
                            Text(strings.noThanks)
                        }
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun AnimatedSkyBackground(modifier: Modifier = Modifier) {
    val isDarkTheme = isSystemInDarkTheme()
    var nightJson by remember { mutableStateOf(cachedNightSkyJson) }
    var dayJson by remember { mutableStateOf(cachedDaySkyJson) }

    LaunchedEffect(isDarkTheme) {
        suspend fun loadJson(path: String): String {
            return withContext(Dispatchers.Default) {
                Res.readBytes(path).decodeToString()
            }
        }

        if (isDarkTheme && nightJson == null) {
            val loaded = loadJson(NIGHT_SKY_JSON_PATH)
            nightJson = loaded
            cachedNightSkyJson = loaded
        } else if (!isDarkTheme && dayJson == null) {
            val loaded = loadJson(DAY_SKY_JSON_PATH)
            dayJson = loaded
            cachedDaySkyJson = loaded
        }

        if (!isDarkTheme && nightJson == null) {
            val loaded = loadJson(NIGHT_SKY_JSON_PATH)
            nightJson = loaded
            cachedNightSkyJson = loaded
        } else if (isDarkTheme && dayJson == null) {
            val loaded = loadJson(DAY_SKY_JSON_PATH)
            dayJson = loaded
            cachedDaySkyJson = loaded
        }
    }

    val json = if (isDarkTheme) nightJson else dayJson
    if (json == null) {
        val fallback = if (isDarkTheme) {
            Brush.verticalGradient(listOf(Color(0xFF0F1630), Color(0xFF1A2346), Color(0xFF26335E)))
        } else {
            Brush.verticalGradient(listOf(Color(0xFF9ED8FF), Color(0xFFC8EBFF), Color(0xFFEAF7FF)))
        }
        Box(
            modifier = modifier.background(fallback),
        )
        return
    }

    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(json)
    }
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = Compottie.IterateForever,
    )

    Image(
        painter = rememberLottiePainter(
            composition = composition,
            progress = { progress },
        ),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier,
    )
}

private enum class StatusBarMode {
    Idle,
    Speaking,
    Damage,
}

@Composable
private fun AnimatedStatusBar(
    mode: StatusBarMode,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "status-bar")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (mode) {
                    StatusBarMode.Idle -> 2600
                    StatusBarMode.Speaking -> 1400
                    StatusBarMode.Damage -> 900
                },
                easing = LinearEasing,
            ),
        ),
        label = "status-bar-phase",
    )

    val colors = when (mode) {
        StatusBarMode.Idle -> listOf(
            Color(0xFF0F6B3A),
            Color(0xFF2ECC71),
            Color(0xFF1ABC9C),
        )
        StatusBarMode.Speaking -> listOf(
            Color(0xFFFF1744),
            Color(0xFFFF9100),
            Color(0xFFFFD600),
            Color(0xFF00E676),
            Color(0xFF00B0FF),
            Color(0xFFD500F9),
        )
        StatusBarMode.Damage -> listOf(
            Color(0xFF7F0000),
            Color(0xFFFF1744),
            Color(0xFFFF5252),
        )
    }

    androidx.compose.foundation.Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val waveCount = 2.5f
        val amplitude = h * 0.25f
        val baseline = h * 0.65f
        val step = 8f.coerceAtMost(w / 60f)

        val shift = w * phase
        val brush = Brush.linearGradient(
            colors = colors,
            start = Offset(shift - w, 0f),
            end = Offset(shift, 0f),
        )

        val path = Path()
        path.moveTo(0f, h)
        var x = 0f
        while (x <= w + step) {
            val progress = x / w
            val angle = 2f * PI.toFloat() * (waveCount * progress + phase)
            val y = (baseline + amplitude * sin(angle)).coerceIn(0f, h)
            path.lineTo(x, y)
            x += step
        }
        path.lineTo(w, h)
        path.close()

        drawPath(path = path, brush = brush)
    }
}
