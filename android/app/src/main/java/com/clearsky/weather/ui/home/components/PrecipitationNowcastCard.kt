package com.clearsky.weather.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.clearsky.weather.R
import com.clearsky.weather.domain.model.Minutely15

@Composable
fun PrecipitationNowcastCard(
    minutely15: List<Minutely15>,
    modifier: Modifier = Modifier
) {
    val hasRain = minutely15.any { it.precipitation > 0 }

    WeatherCard(
        title = stringResource(R.string.precipitation),
        modifier = modifier
    ) {
        Column {
            Text(
                text = if (hasRain) stringResource(R.string.rain_expected) else stringResource(R.string.no_precipitation),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = stringResource(R.string.next_minutes_format, minutely15.size * 15),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            PrecipitationBarChart(
                data = minutely15,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.now),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Text(
                    text = "${minutely15.size * 15}m",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun PrecipitationBarChart(
    data: List<Minutely15>,
    modifier: Modifier = Modifier
) {
    val maxPrecip = data.maxOfOrNull { it.precipitation }?.coerceAtLeast(1.0) ?: 1.0

    Canvas(modifier = modifier) {
        val barCount = data.size
        if (barCount == 0) return@Canvas

        val barWidth = (size.width / barCount) * 0.7f
        val gap = (size.width / barCount) * 0.3f

        data.forEachIndexed { index, item ->
            val barHeight = ((item.precipitation / maxPrecip) * size.height).toFloat()
                .coerceAtLeast(if (item.precipitation > 0) 4f else 0f)

            val x = index * (barWidth + gap)
            val y = size.height - barHeight

            val color = when {
                item.precipitation > 5.0 -> Color(0xFF1565C0)
                item.precipitation > 2.0 -> Color(0xFF42A5F5)
                item.precipitation > 0.5 -> Color(0xFF90CAF9)
                item.precipitation > 0 -> Color(0xFFBBDEFB)
                else -> Color.White.copy(alpha = 0.1f)
            }

            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight.coerceAtLeast(2f)),
                cornerRadius = CornerRadius(2f)
            )
        }
    }
}
