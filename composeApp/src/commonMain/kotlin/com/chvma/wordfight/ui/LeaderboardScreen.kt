package com.chvma.wordfight.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chvma.wordfight.ads.BannerAdView
import com.chvma.wordfight.leaderboard.LeaderboardEntry
import com.chvma.wordfight.leaderboard.LeaderboardPeriod
import com.chvma.wordfight.localization.AppStrings
import kotlinx.coroutines.delay

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
                .padding(paddingValues),
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
                    val rows = remember(entries) { buildLeaderboardRows(entries) }
                    val listState = rememberLazyListState()
                    val currentPlayerIndex = remember(rows) {
                        rows.indexOfFirst {
                            it is LeaderboardRowItem.Entry && it.entry.isCurrentPlayer
                        }
                    }
                    val highlight = remember { Animatable(0f) }

                    LaunchedEffect(rows, currentPlayerIndex) {
                        if (currentPlayerIndex < 0) return@LaunchedEffect
                        delay(1000)
                        listState.animateScrollToItem(currentPlayerIndex)
                        repeat(3) {
                            highlight.animateTo(1f, tween(durationMillis = 250))
                            highlight.animateTo(0f, tween(durationMillis = 250))
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(
                            items = rows,
                            key = { row ->
                                when (row) {
                                    is LeaderboardRowItem.Entry -> row.entry.playerId
                                    is LeaderboardRowItem.Ellipsis -> "ellipsis_${row.afterRank}"
                                }
                            },
                        ) { row ->
                            when (row) {
                                is LeaderboardRowItem.Entry -> LeaderboardRow(
                                    entry = row.entry,
                                    strings = strings,
                                    highlight = if (row.entry.isCurrentPlayer) highlight.value else 0f,
                                )

                                is LeaderboardRowItem.Ellipsis -> LeaderboardEllipsis()
                            }
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
    highlight: Float = 0f,
) {
    val containerColor = if (entry.isCurrentPlayer) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f + 0.44f * highlight)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(containerColor, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${entry.rank}. ${entry.name}",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = if (entry.isCurrentPlayer) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 17.sp,
            )
            Text(
                text = "${strings.scoreLabel}: ${entry.score}",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            )
        }
        Text(
            text = "${strings.leaderboardLanguage}: ${entry.language.nativeName}",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun LeaderboardEllipsis() {
    Text(
        text = "•••",
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
    )
}

private sealed interface LeaderboardRowItem {
    data class Entry(val entry: LeaderboardEntry) : LeaderboardRowItem
    data class Ellipsis(val afterRank: Int) : LeaderboardRowItem
}

/**
 * Flattens leaderboard entries into display rows, inserting an ellipsis wherever
 * the rank is not contiguous (e.g. between the top podium and the window around
 * the current player).
 */
private fun buildLeaderboardRows(entries: List<LeaderboardEntry>): List<LeaderboardRowItem> {
    return buildList {
        entries.forEachIndexed { index, entry ->
            if (index > 0 && entry.rank > entries[index - 1].rank + 1) {
                add(LeaderboardRowItem.Ellipsis(afterRank = entries[index - 1].rank))
            }
            add(LeaderboardRowItem.Entry(entry))
        }
    }
}
