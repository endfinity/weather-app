package com.clearsky.weather.ui.util

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedWeatherIcon(
    weatherCode: Int,
    isDay: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    when {
        weatherCode == 0 -> if (isDay) AnimatedSun(modifier, tint) else AnimatedMoon(modifier, tint)
        weatherCode in 1..2 -> if (isDay) AnimatedPartlyCloudy(modifier, tint) else AnimatedCloudyNight(modifier, tint)
        weatherCode == 3 -> AnimatedOvercast(modifier, tint)
        weatherCode in 45..48 -> AnimatedFog(modifier, tint)
        weatherCode in 51..67 -> AnimatedRain(modifier, tint)
        weatherCode in 71..77 || weatherCode in 85..86 -> AnimatedSnow(modifier, tint)
        weatherCode in 80..82 -> AnimatedRain(modifier, tint)
        weatherCode in 95..99 -> AnimatedThunderstorm(modifier, tint)
        else -> if (isDay) AnimatedSun(modifier, tint) else AnimatedMoon(modifier, tint)
    }
}

@Composable
private fun AnimatedSun(modifier: Modifier = Modifier, tint: Color = Color.White) {
    val transition = rememberInfiniteTransition(label = "sun")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing)),
        label = "sunRotation"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "sunPulse"
    )

    Canvas(modifier = modifier.size(40.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension * 0.2f

        drawCircle(color = tint, radius = radius * pulse, center = center)

        val rayCount = 8
        val rayInner = radius * 1.5f
        val rayOuter = radius * 2.2f * pulse
        rotate(rotation, center) {
            for (i in 0 until rayCount) {
                val angle = (i * 360f / rayCount) * (PI.toFloat() / 180f)
                val startX = center.x + cos(angle) * rayInner
                val startY = center.y + sin(angle) * rayInner
                val endX = center.x + cos(angle) * rayOuter
                val endY = center.y + sin(angle) * rayOuter
                drawLine(
                    color = tint,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = size.minDimension * 0.04f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun AnimatedMoon(modifier: Modifier = Modifier, tint: Color = Color.White) {
    val transition = rememberInfiniteTransition(label = "moon")
    val glow by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Reverse),
        label = "moonGlow"
    )

    Canvas(modifier = modifier.size(40.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension * 0.3f

        drawCircle(
            color = tint.copy(alpha = 0.15f * glow),
            radius = radius * 1.3f,
            center = center
        )

        drawCircle(color = tint, radius = radius, center = center)

        drawCircle(
            color = Color.Transparent,
            radius = radius * 0.75f,
            center = Offset(center.x + radius * 0.35f, center.y - radius * 0.25f)
        )
        drawCircle(
            color = tint.copy(alpha = 0f),
            radius = radius * 0.7f,
            center = Offset(center.x + radius * 0.4f, center.y - radius * 0.3f)
        )
    }
}

@Composable
private fun AnimatedPartlyCloudy(modifier: Modifier = Modifier, tint: Color = Color.White) {
    val transition = rememberInfiniteTransition(label = "partlyCloudy")
    val sunRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing)),
        label = "pcSunRotation"
    )
    val cloudDrift by transition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Reverse),
        label = "cloudDrift"
    )

    Canvas(modifier = modifier.size(40.dp)) {
        val w = size.width
        val h = size.height
        val sunCenter = Offset(w * 0.65f, h * 0.3f)
        val sunRadius = w * 0.12f

        drawCircle(color = tint, radius = sunRadius, center = sunCenter)
        val rayCount = 6
        val rayInner = sunRadius * 1.4f
        val rayOuter = sunRadius * 2.0f
        rotate(sunRotation, sunCenter) {
            for (i in 0 until rayCount) {
                val angle = (i * 360f / rayCount) * (PI.toFloat() / 180f)
                drawLine(
                    color = tint,
                    start = Offset(sunCenter.x + cos(angle) * rayInner, sunCenter.y + sin(angle) * rayInner),
                    end = Offset(sunCenter.x + cos(angle) * rayOuter, sunCenter.y + sin(angle) * rayOuter),
                    strokeWidth = w * 0.03f,
                    cap = StrokeCap.Round
                )
            }
        }

        drawCloud(this, tint, Offset(w * 0.4f + cloudDrift, h * 0.55f), w * 0.5f)
    }
}

@Composable
private fun AnimatedCloudyNight(modifier: Modifier = Modifier, tint: Color = Color.White) {
    val transition = rememberInfiniteTransition(label = "cloudyNight")
    val cloudDrift by transition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Reverse),
        label = "cnCloudDrift"
    )
    val glow by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Reverse),
        label = "cnMoonGlow"
    )

    Canvas(modifier = modifier.size(40.dp)) {
        val w = size.width
        val h = size.height
        val moonCenter = Offset(w * 0.65f, h * 0.3f)
        val moonRadius = w * 0.12f

        drawCircle(color = tint.copy(alpha = 0.15f * glow), radius = moonRadius * 1.3f, center = moonCenter)
        drawCircle(color = tint, radius = moonRadius, center = moonCenter)

        drawCloud(this, tint, Offset(w * 0.4f + cloudDrift, h * 0.55f), w * 0.5f)
    }
}

@Composable
private fun AnimatedOvercast(modifier: Modifier = Modifier, tint: Color = Color.White) {
    val transition = rememberInfiniteTransition(label = "overcast")
    val drift1 by transition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(5000), RepeatMode.Reverse),
        label = "ocDrift1"
    )
    val drift2 by transition.animateFloat(
        initialValue = 2f,
        targetValue = -2f,
        animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Reverse),
        label = "ocDrift2"
    )

    Canvas(modifier = modifier.size(40.dp)) {
        val w = size.width
        val h = size.height
        drawCloud(this, tint.copy(alpha = 0.6f), Offset(w * 0.35f + drift2, h * 0.35f), w * 0.4f)
        drawCloud(this, tint, Offset(w * 0.45f + drift1, h * 0.55f), w * 0.55f)
    }
}

@Composable
private fun AnimatedFog(modifier: Modifier = Modifier, tint: Color = Color.White) {
    val transition = rememberInfiniteTransition(label = "fog")
    val drift by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Reverse),
        label = "fogDrift"
    )

    Canvas(modifier = modifier.size(40.dp)) {
        val w = size.width
        val h = size.height
        val lineWidth = w * 0.04f
        val alphas = listOf(0.9f, 0.7f, 0.5f, 0.35f)
        val yPositions = listOf(0.3f, 0.45f, 0.6f, 0.75f)

        yPositions.forEachIndexed { idx, yFrac ->
            val offset = drift * (if (idx % 2 == 0) 1f else -0.7f)
            drawLine(
                color = tint.copy(alpha = alphas[idx]),
                start = Offset(w * 0.15f + offset, h * yFrac),
                end = Offset(w * 0.85f + offset, h * yFrac),
                strokeWidth = lineWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun AnimatedRain(modifier: Modifier = Modifier, tint: Color = Color.White) {
    val transition = rememberInfiniteTransition(label = "rain")
    val fall by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "rainFall"
    )

    Canvas(modifier = modifier.size(40.dp)) {
        val w = size.width
        val h = size.height

        drawCloud(this, tint, Offset(w * 0.45f, h * 0.3f), w * 0.5f)

        val dropXPositions = listOf(0.3f, 0.5f, 0.7f)
        val dropPhases = listOf(0f, 0.33f, 0.66f)

        dropXPositions.forEachIndexed { idx, xFrac ->
            val phase = (fall + dropPhases[idx]) % 1f
            val y = h * 0.5f + phase * h * 0.4f
            val alpha = 1f - phase
            drawLine(
                color = tint.copy(alpha = alpha * 0.8f),
                start = Offset(w * xFrac, y),
                end = Offset(w * xFrac - w * 0.02f, y + h * 0.08f),
                strokeWidth = w * 0.03f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun AnimatedSnow(modifier: Modifier = Modifier, tint: Color = Color.White) {
    val transition = rememberInfiniteTransition(label = "snow")
    val fall by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing)),
        label = "snowFall"
    )
    val sway by transition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "snowSway"
    )

    Canvas(modifier = modifier.size(40.dp)) {
        val w = size.width
        val h = size.height

        drawCloud(this, tint, Offset(w * 0.45f, h * 0.3f), w * 0.5f)

        val flakeXPositions = listOf(0.25f, 0.45f, 0.65f, 0.55f)
        val flakePhases = listOf(0f, 0.4f, 0.2f, 0.7f)

        flakeXPositions.forEachIndexed { idx, xFrac ->
            val phase = (fall + flakePhases[idx]) % 1f
            val x = w * xFrac + sway * (if (idx % 2 == 0) 1f else -1f)
            val y = h * 0.5f + phase * h * 0.4f
            val alpha = 1f - phase

            drawCircle(
                color = tint.copy(alpha = alpha * 0.9f),
                radius = w * 0.025f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun AnimatedThunderstorm(modifier: Modifier = Modifier, tint: Color = Color.White) {
    val transition = rememberInfiniteTransition(label = "storm")
    val flash by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "lightning"
    )
    val rainFall by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing)),
        label = "stormRain"
    )

    Canvas(modifier = modifier.size(40.dp)) {
        val w = size.width
        val h = size.height

        drawCloud(this, tint, Offset(w * 0.45f, h * 0.25f), w * 0.55f)

        val boltAlpha = if (flash in 0.15f..0.25f || flash in 0.4f..0.45f) 1f else 0f
        if (boltAlpha > 0f) {
            val boltPath = Path().apply {
                moveTo(w * 0.5f, h * 0.4f)
                lineTo(w * 0.42f, h * 0.6f)
                lineTo(w * 0.52f, h * 0.6f)
                lineTo(w * 0.45f, h * 0.82f)
            }
            drawPath(
                path = boltPath,
                color = Color(0xFFFFD54F).copy(alpha = boltAlpha),
                style = Stroke(width = w * 0.04f, cap = StrokeCap.Round)
            )
        }

        val dropXPositions = listOf(0.28f, 0.65f)
        val dropPhases = listOf(0f, 0.5f)
        dropXPositions.forEachIndexed { idx, xFrac ->
            val phase = (rainFall + dropPhases[idx]) % 1f
            val y = h * 0.45f + phase * h * 0.4f
            val alpha = 1f - phase
            drawLine(
                color = tint.copy(alpha = alpha * 0.7f),
                start = Offset(w * xFrac, y),
                end = Offset(w * xFrac - w * 0.02f, y + h * 0.07f),
                strokeWidth = w * 0.025f,
                cap = StrokeCap.Round
            )
        }
    }
}

private fun drawCloud(drawScope: DrawScope, color: Color, center: Offset, width: Float) {
    val h = width * 0.45f
    val r1 = width * 0.25f
    val r2 = width * 0.35f
    val r3 = width * 0.22f

    drawScope.drawCircle(color = color, radius = r1, center = Offset(center.x - width * 0.2f, center.y))
    drawScope.drawCircle(color = color, radius = r2, center = Offset(center.x + width * 0.05f, center.y - h * 0.2f))
    drawScope.drawCircle(color = color, radius = r3, center = Offset(center.x + width * 0.25f, center.y))
    drawScope.drawOval(
        color = color,
        topLeft = Offset(center.x - width * 0.35f, center.y - h * 0.1f),
        size = Size(width * 0.7f, h * 0.55f)
    )
}
