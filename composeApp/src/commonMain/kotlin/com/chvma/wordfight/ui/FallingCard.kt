package com.chvma.wordfight.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.chvma.wordfight.model.Card
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

@Composable
private fun cardPainter(imageKey: Int): Painter = painterResource(
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
        else -> Res.drawable._1
    }
)

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
            painter = cardPainter(card.imageKey),
            contentDescription = card.word,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .offset(x = xPx.toDp(), y = yPx.toDp())
                .size(cardWidthDp, cardHeightDp),
        )
    }
}
