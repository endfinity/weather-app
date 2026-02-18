package com.clearsky.weather.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.clearsky.weather.R
import com.clearsky.weather.ui.util.FormatUtil

@Composable
fun UvIndexCard(
    uvIndex: Double,
    modifier: Modifier = Modifier
) {
    val level = FormatUtil.uvIndexLevel(uvIndex)
    val uvDesc = "UV index ${FormatUtil.formatUvIndex(uvIndex)}, $level. ${uvAdvice(uvIndex)}"

    WeatherCard(
        title = stringResource(R.string.uv_index),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.clearAndSetSemantics { contentDescription = uvDesc }
        ) {
            Text(
                text = FormatUtil.formatUvIndex(uvIndex),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = level,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            UvScaleBar(uvIndex = uvIndex)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = uvAdvice(uvIndex),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun UvScaleBar(
    uvIndex: Double,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Color(0xFF4CAF50),
        Color(0xFFFFEB3B),
        Color(0xFFFF9800),
        Color(0xFFF44336),
        Color(0xFF9C27B0)
    )
    val fraction = (uvIndex / 11.0).coerceIn(0.0, 1.0).toFloat()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
    ) {
        val barWidth = size.width
        val barHeight = size.height
        val segmentWidth = barWidth / colors.size

        colors.forEachIndexed { index, color ->
            drawRoundRect(
                color = color,
                topLeft = Offset(index * segmentWidth, 0f),
                size = Size(segmentWidth, barHeight),
                cornerRadius = if (index == 0 || index == colors.lastIndex)
                    CornerRadius(barHeight / 2) else CornerRadius.Zero
            )
        }

        val indicatorX = fraction * barWidth
        drawCircle(
            color = Color.White,
            radius = barHeight,
            center = Offset(indicatorX.coerceIn(barHeight, barWidth - barHeight), barHeight / 2)
        )
    }
}

@Composable
private fun uvAdvice(uv: Double): String = when {
    uv <= 2 -> stringResource(R.string.uv_no_protection)
    uv <= 5 -> stringResource(R.string.uv_wear_sunscreen)
    uv <= 7 -> stringResource(R.string.uv_reduce_exposure)
    uv <= 10 -> stringResource(R.string.uv_extra_protection)
    else -> stringResource(R.string.uv_stay_indoors)
}
