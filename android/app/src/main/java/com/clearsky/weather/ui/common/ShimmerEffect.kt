package com.clearsky.weather.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue = -300f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    return Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.08f),
            Color.White.copy(alpha = 0.20f),
            Color.White.copy(alpha = 0.08f)
        ),
        start = Offset(translateX, 0f),
        end = Offset(translateX + 300f, 0f)
    )
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    cornerRadius: Dp = 8.dp
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(ShimmerBrush())
    )
}

@Composable
fun WeatherLoadingSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ShimmerBox(
            modifier = Modifier.width(120.dp),
            height = 20.dp
        )

        ShimmerBox(
            modifier = Modifier.size(96.dp),
            height = 96.dp,
            cornerRadius = 48.dp
        )

        ShimmerBox(
            modifier = Modifier.width(160.dp),
            height = 14.dp
        )

        Spacer(modifier = Modifier.height(8.dp))

        ShimmerBox(
            modifier = Modifier.fillMaxWidth(),
            height = 80.dp,
            cornerRadius = 16.dp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShimmerBox(
                modifier = Modifier.weight(1f),
                height = 120.dp,
                cornerRadius = 16.dp
            )
            ShimmerBox(
                modifier = Modifier.weight(1f),
                height = 120.dp,
                cornerRadius = 16.dp
            )
        }

        ShimmerBox(
            modifier = Modifier.fillMaxWidth(),
            height = 140.dp,
            cornerRadius = 16.dp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShimmerBox(
                modifier = Modifier.weight(1f),
                height = 100.dp,
                cornerRadius = 16.dp
            )
            ShimmerBox(
                modifier = Modifier.weight(1f),
                height = 100.dp,
                cornerRadius = 16.dp
            )
        }
    }
}