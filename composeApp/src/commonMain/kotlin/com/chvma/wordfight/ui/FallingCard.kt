package com.chvma.wordfight.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.chvma.wordfight.model.Card
import org.jetbrains.compose.resources.painterResource

@Composable
fun FallingCard(
    card: Card,
    screenWidth: Float,
    screenHeight: Float,
) {
    val density = LocalDensity.current
    val cardWidthDp = 110.dp
    val cardHeightDp = 110.dp

    with(density) {
        val xPx = card.x * screenWidth - cardWidthDp.toPx() / 2
        val yPx = card.y * screenHeight - cardHeightDp.toPx() / 2

        Image(
            painter = painterResource(card.content.res),
            contentDescription = card.content.word,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .offset(x = xPx.toDp(), y = yPx.toDp())
                .size(cardWidthDp, cardHeightDp),
        )
    }
}
