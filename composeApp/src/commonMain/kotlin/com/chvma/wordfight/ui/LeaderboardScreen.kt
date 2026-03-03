package com.chvma.wordfight.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chvma.wordfight.ads.BannerAdView
import com.chvma.wordfight.leaderboard.LeaderboardEntry
import com.chvma.wordfight.leaderboard.LeaderboardPeriod
import com.chvma.wordfight.localization.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    selectedPeriod: LeaderboardPeriod,
    entries: List<LeaderboardEntry>,
    loading: Boolean,
    onSelectPeriod: (LeaderboardPeriod) -> Unit,
    onBack: () -> Unit,
    musicEnabled: Boolean,
    onToggleMusic: () -> Unit,
    strings: AppStrings,
) {
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
                        text = strings.leaderboard,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
        ) {
            TabRow(selectedTabIndex = if (selectedPeriod == LeaderboardPeriod.TODAY) 0 else 1) {
                Tab(
                    selected = selectedPeriod == LeaderboardPeriod.TODAY,
                    onClick = { onSelectPeriod(LeaderboardPeriod.TODAY) },
                    text = { Text(strings.leaderboardToday) },
                )
                Tab(
                    selected = selectedPeriod == LeaderboardPeriod.ALL_TIME,
                    onClick = { onSelectPeriod(LeaderboardPeriod.ALL_TIME) },
                    text = { Text(strings.leaderboardAllTime) },
                )
            }

            when {
                loading -> {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = strings.leaderboardLoading,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }

                entries.isEmpty() -> {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = strings.leaderboardEmpty,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(entries) { entry ->
                            LeaderboardRow(
                                entry = entry,
                                strings = strings,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(
    entry: LeaderboardEntry,
    strings: AppStrings,
) {
    val containerColor = if (entry.isCurrentPlayer) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = "${entry.rank}. ${entry.name}",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = if (entry.isCurrentPlayer) FontWeight.Bold else FontWeight.SemiBold,
            fontSize = 17.sp,
        )
        Text(
            text = "${strings.leaderboardLanguage}: ${entry.language.nativeName}",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontSize = 13.sp,
        )
    }
}
