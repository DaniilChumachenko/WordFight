package com.chvma.wordfight.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/** Animated fill style for [GradientButton]. */
enum class ButtonEffect {
    Aurora,       // drifting iridescent colour blobs
    Holographic,  // diagonal rainbow foil bands
    Plasma,       // flowing liquid-metal sheen
    Sparkle,      // twinkling drifting glitter
    Wave,         // colour liquid sloshing at the bottom
    NeonBorder,   // dark fill with a glowing dot running along a 2dp outline
}

private const val TWO_PI = (2.0 * PI).toFloat()

/**
 * App-wide button with a rich animated fill. The look is chosen with [effect],
 * coloured from a single [baseColor], and varied per instance by [seed] (motion
 * + speed) so no two buttons animate alike.
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    baseColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    effect: ButtonEffect = ButtonEffect.Aurora,
    seed: Int = 0,
    shape: RoundedCornerShape = RoundedCornerShape(percent = 50),
    contentColor: Color = Color.White,
    contentPadding: PaddingValues = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
) {
    val transition = rememberInfiniteTransition(label = "fx-button")
    // Two seamless trig phases (sin/cos of N·2π match at the loop boundary) and
    // one linear 0..1 phase. Speeds jittered by seed so buttons fall out of sync.
    val p1 by transition.animateFloat(
        initialValue = 0f, targetValue = TWO_PI,
        animationSpec = infiniteRepeatable(tween(7000 + (seed % 5) * 600, easing = LinearEasing)),
        label = "fx-p1",
    )
    val p2 by transition.animateFloat(
        initialValue = 0f, targetValue = TWO_PI,
        animationSpec = infiniteRepeatable(tween(11000 + (seed % 4) * 800, easing = LinearEasing)),
        label = "fx-p2",
    )
    val pLin by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6000 + (seed % 6) * 500, easing = LinearEasing)),
        label = "fx-lin",
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.5f)
            .clip(shape)
            .drawBehind {
                when (effect) {
                    ButtonEffect.Aurora -> drawAurora(baseColor, seed, p1, p2)
                    ButtonEffect.Holographic -> drawHolographic(baseColor, seed, pLin)
                    ButtonEffect.Plasma -> drawPlasma(baseColor, p1, p2)
                    ButtonEffect.Sparkle -> drawSparkle(baseColor, seed, p1, pLin)
                    ButtonEffect.Wave -> drawWave(baseColor, p1, p2)
                    ButtonEffect.NeonBorder -> drawNeonBorder(baseColor, pLin * 360f)
                }
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = contentColor,
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                // Keeps the label readable over any bright effect.
                shadow = Shadow(color = Color(0xCC000000), blurRadius = 6f),
            ),
        )
    }
}

// ---- Effects -------------------------------------------------------------

private fun DrawScope.drawAurora(base: Color, seed: Int, p1: Float, p2: Float) {
    drawRect(color = lerp(base, Color.Black, 0.80f))
    val colors = listOf(
        lerp(base, Color.White, 0.40f),
        base,
        base.shiftHue(34f),
        base.shiftHue(-30f),
    )
    colors.forEachIndexed { i, color ->
        val o = ((seed * 53 + i * 97) % 360) / 360f * TWO_PI
        val cx = 0.30f + 0.40f * ((i * 2 + seed) % 3) / 2f
        val cy = if (i % 2 == 0) 0.38f else 0.64f
        val fx = 1 + i % 2
        val fy = 1 + (i + 1) % 2
        val x = (cx + 0.24f * sin(fx * p1 + o)) * size.width
        val y = (cy + 0.20f * cos(fy * p2 + o)) * size.height
        val r = 0.85f * size.minDimension
        val center = Offset(x, y)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(color.copy(alpha = 0.65f), color.copy(alpha = 0f)),
                center = center,
                radius = r,
            ),
            radius = r,
            center = center,
        )
    }
}

private fun DrawScope.drawHolographic(base: Color, seed: Int, pLin: Float) {
    drawRect(color = lerp(base, Color.Black, 0.70f))
    // Full hue wheel around the base (first == last hue → seamless repeat).
    val hues = (0..6).map { base.shiftHue(it * 60f).copy(alpha = 0.55f) }
    val tile = size.width
    val shift = ((pLin + seed * 0.13f) % 1f) * tile
    drawRect(
        brush = Brush.linearGradient(
            colors = hues,
            start = Offset(shift, 0f),
            end = Offset(shift + tile, size.height),
            tileMode = TileMode.Repeated,
        ),
    )
}

private fun DrawScope.drawPlasma(base: Color, p1: Float, p2: Float) {
    val light = lerp(base, Color.White, 0.65f)
    val dark = lerp(base, Color.Black, 0.75f)
    drawRect(color = lerp(base, Color.Black, 0.45f))

    // Diagonal light band sweeping back and forth (sin → seamless).
    val s1 = sin(p1) * 0.5f + 0.5f
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(dark, base, light, base, dark),
            start = Offset(-size.width + 2f * size.width * s1, 0f),
            end = Offset(2f * size.width * s1, size.height),
        ),
    )
    // Softer counter-sweep for the marbled, liquid feel.
    val s2 = cos(p2) * 0.5f + 0.5f
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(dark.copy(alpha = 0f), light.copy(alpha = 0.45f), dark.copy(alpha = 0f)),
            start = Offset(size.width, -size.height + 2f * size.height * s2),
            end = Offset(0f, 2f * size.height * s2),
        ),
    )
}

private fun DrawScope.drawSparkle(base: Color, seed: Int, p1: Float, pLin: Float) {
    drawRect(color = lerp(base, Color.Black, 0.78f))
    // Gentle base glow so the dark fill is not flat.
    drawCircle(
        brush = Brush.radialGradient(
            listOf(base.copy(alpha = 0.35f), base.copy(alpha = 0f)),
            center = Offset(size.width * 0.5f, size.height * 0.5f),
            radius = size.maxDimension * 0.6f,
        ),
        radius = size.maxDimension * 0.6f,
        center = Offset(size.width * 0.5f, size.height * 0.5f),
    )
    val count = 16
    for (i in 0 until count) {
        val rx = hash(seed, i, 1)
        val drift = (hash(seed, i, 2) - (pLin + i * 0.045f))
        val ry = drift - floor(drift) // wrap to 0..1, drifting upward
        val x = rx * size.width
        val y = ry * size.height
        val twinkle = sin(p1 * (1f + (i % 3)) + i) * 0.5f + 0.5f
        val rad = (0.010f + 0.022f * hash(seed, i, 3)) * size.minDimension * (0.4f + 0.6f * twinkle)
        val sparkColor = if (i % 4 == 0) base.shiftHue(40f) else Color.White
        drawCircle(
            color = sparkColor.copy(alpha = 0.25f + 0.75f * twinkle),
            radius = rad,
            center = Offset(x, y),
        )
    }
}

private fun DrawScope.drawWave(base: Color, p1: Float, p2: Float) {
    drawRect(color = lerp(base, Color.Black, 0.82f))
    val level = 0.55f + 0.06f * sin(p2)
    val baseY = size.height * (1f - level)
    val amp = size.height * 0.10f
    val waves = 1.6f
    val step = max(6f, size.width / 48f)

    val path = Path().apply {
        moveTo(0f, size.height)
        lineTo(0f, baseY)
        var x = 0f
        while (x <= size.width + step) {
            val yy = baseY + amp * sin((x / size.width) * TWO_PI * waves + p1)
            lineTo(x, yy)
            x += step
        }
        lineTo(size.width, size.height)
        close()
    }
    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(lerp(base, Color.White, 0.25f), base, lerp(base, Color.Black, 0.25f)),
            startY = baseY - amp,
            endY = size.height,
        ),
    )
}

private fun DrawScope.drawNeonBorder(base: Color, angleDeg: Float) {
    val dim = base.copy(alpha = 0.35f)
    val bright = lerp(base, Color.White, 0.35f)
    val fill = lerp(base, Color.Black, 0.82f)
    val strokePx = 2.dp.toPx()
    val corner = CornerRadius(size.minDimension / 2f)

    drawRoundRect(color = fill, cornerRadius = corner)

    // Rotating sweep gradient masked down to just the 2dp stroke ring.
    with(drawContext.canvas) { saveLayer(Rect(0f, 0f, size.width, size.height), Paint()) }
    val span = size.maxDimension * 2f
    rotate(degrees = angleDeg) {
        drawRect(
            brush = Brush.sweepGradient(
                0.00f to dim,
                0.42f to dim,
                0.50f to bright,
                0.58f to dim,
                1.00f to dim,
                center = center,
            ),
            topLeft = Offset(center.x - span / 2f, center.y - span / 2f),
            size = Size(span, span),
        )
    }
    drawRoundRect(
        color = Color.Black,
        topLeft = Offset(strokePx / 2f, strokePx / 2f),
        size = Size(size.width - strokePx, size.height - strokePx),
        cornerRadius = CornerRadius((size.minDimension - strokePx) / 2f),
        style = Stroke(width = strokePx),
        blendMode = BlendMode.DstIn,
    )
    drawContext.canvas.restore()
}

// ---- Helpers -------------------------------------------------------------

/** Deterministic pseudo-random in 0..1 from three integer coordinates. */
private fun hash(a: Int, b: Int, c: Int): Float {
    val v = sin(a * 12.9898 + b * 78.233 + c * 37.719) * 43758.5453
    return (v - floor(v)).toFloat()
}

/** Rotates a colour's hue by [degrees] (keeps saturation/value/alpha). */
private fun Color.shiftHue(degrees: Float): Color {
    val r = red
    val g = green
    val b = blue
    val mx = max(r, max(g, b))
    val mn = min(r, min(g, b))
    val d = mx - mn

    var h = when {
        d == 0f -> 0f
        mx == r -> 60f * (((g - b) / d) % 6f)
        mx == g -> 60f * (((b - r) / d) + 2f)
        else -> 60f * (((r - g) / d) + 4f)
    }
    if (h < 0f) h += 360f
    val s = if (mx == 0f) 0f else d / mx
    val v = mx

    var nh = (h + degrees) % 360f
    if (nh < 0f) nh += 360f
    val c = v * s
    val x = c * (1f - abs((nh / 60f) % 2f - 1f))
    val m = v - c
    val (r1, g1, b1) = when {
        nh < 60f -> Triple(c, x, 0f)
        nh < 120f -> Triple(x, c, 0f)
        nh < 180f -> Triple(0f, c, x)
        nh < 240f -> Triple(0f, x, c)
        nh < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    return Color(r1 + m, g1 + m, b1 + m, alpha)
}
