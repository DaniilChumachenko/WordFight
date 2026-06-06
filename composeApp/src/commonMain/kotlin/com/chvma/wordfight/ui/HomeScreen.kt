package com.chvma.wordfight.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chvma.wordfight.ads.BannerAdView
import com.chvma.wordfight.localization.AppStrings
import com.chvma.wordfight.speech.rememberPermissionRequester

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    bestScore: Int,
    onStartGame: () -> Unit,
    onCategories: () -> Unit,
    onMyWords: () -> Unit,
    onLanguages: () -> Unit,
    onLeaderboard: () -> Unit,
    hasPermission: Boolean,
    onPermissionGranted: () -> Unit,
    musicEnabled: Boolean,
    onToggleMusic: () -> Unit,
    strings: AppStrings,
) {
    val permissionRequester = rememberPermissionRequester { granted ->
        if (granted) {
            onPermissionGranted()
            onStartGame()
        }
    }

    fun startGameFromTap() {
        if (hasPermission) {
            onStartGame()
        } else {
            permissionRequester.requestPermission { granted ->
                if (granted) {
                    onStartGame()
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
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
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    startGameFromTap()
                },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = strings.appTitle,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 44.sp,
                    lineHeight = 56.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(30.dp))

                Text(
                    text = "${strings.bestLabel}: $bestScore",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = strings.tapToStart,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(Modifier.height(48.dp))

                val homeButtonModifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.72f)

                GradientButton(
                    text = strings.categories,
                    onClick = onCategories,
                    baseColor = Color(0xFF17D71D),
                    effect = ButtonEffect.Sparkle,
                    seed = 0,
                    modifier = homeButtonModifier,
                )

                Spacer(Modifier.height(16.dp))

                GradientButton(
                    text = strings.myWords,
                    onClick = onMyWords,
                    baseColor = Color(0xFF0D7BD7),
                    effect = ButtonEffect.Sparkle,
                    seed = 1,
                    modifier = homeButtonModifier,
                )

                Spacer(Modifier.height(16.dp))

                GradientButton(
                    text = strings.languages,
                    onClick = onLanguages,
                    baseColor = Color(0xFFE58900),
                    effect = ButtonEffect.Sparkle,
                    seed = 2,
                    modifier = homeButtonModifier,
                )

                Spacer(Modifier.height(16.dp))

                GradientButton(
                    text = strings.leaderboard,
                    onClick = onLeaderboard,
                    baseColor = Color(0xFFBB17DB),
                    effect = ButtonEffect.Sparkle,
                    seed = 3,
                    modifier = homeButtonModifier,
                )
            }
        }
    }
}
