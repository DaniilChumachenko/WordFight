package com.chvma.wordfight.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chvma.wordfight.speech.rememberPermissionRequester
import com.chvma.wordfight.storage.createWordStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(
    onStartGame: () -> Unit,
    onMyWords: () -> Unit,
    hasPermission: Boolean,
    onPermissionGranted: () -> Unit,
) {
    val wordStorage = remember { createWordStorage() }
    var bestScore by remember { mutableStateOf(0) }
    
    val permissionRequester = rememberPermissionRequester { granted ->
        if (granted) {
            onPermissionGranted()
            onStartGame()
        }
    }

    LaunchedEffect(Unit) {
        bestScore = withContext(Dispatchers.Default) {
            wordStorage.getBestScore()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
            .safeContentPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Word Fight!",
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(64.dp))

            Text(
                text = "Best: $bestScore",
                color = Color(0xFFFFD700),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .clickable {
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
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Tap for start!",
                    color = Color(0xFF4CAF50),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = onMyWords,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
            ) {
                Text(
                    text = "My words",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )
            }
        }
    }
}
