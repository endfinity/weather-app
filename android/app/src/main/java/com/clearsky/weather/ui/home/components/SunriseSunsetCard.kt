package com.clearsky.weather.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.clearsky.weather.R
import com.clearsky.weather.domain.model.TimeFormat
import com.clearsky.weather.ui.util.FormatUtil
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun SunriseSunsetCard(
    sunrise: String,
    sunset: String,
    currentTime: String,
    timeFormat: TimeFormat = TimeFormat.H12,
    modifier: Modifier = Modifier
) {
    val sunriseTime = parseTime(sunrise)
    val sunsetTime = parseTime(sunset)
    val currentTimeObj = parseTime(currentTime)

    val dayFraction = if (sunriseTime != null && sunsetTime != null && currentTimeObj != null) {
        val dayLength = java.time.Duration.between(sunriseTime, sunsetTime).toMinutes().toFloat()
        val elapsed = java.time.Duration.between(sunriseTime, currentTimeObj).toMinutes().toFloat()
        (elapsed / dayLength).coerceIn(0f, 1f)
    } else {
        0.5f
    }

    val sunriseFormatted = FormatUtil.formatTime(sunrise, timeFormat)
    val sunsetFormatted = FormatUtil.formatTime(sunset, timeFormat)
    val progressPct = (dayFraction * 100).toInt()
    val arcDesc = "Sun arc showing $progressPct percent of daylight elapsed. " +
        "Sunrise at $sunriseFormatted, sunset at $sunsetFormatted."

    WeatherCard(
        title = stringResource(R.string.sunrise_sunset),
        modifier = modifier
    ) {
        Column {
            SunArcCanvas(
                dayFraction = dayFraction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .semantics { contentDescription = arcDesc }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.WbSunny,
                        contentDescription = stringResource(R.string.sunrise),
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.height(16.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.sunrise),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = FormatUtil.formatTime(sunrise, timeFormat),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.WbTwilight,
                        contentDescription = stringResource(R.string.sunset),
                        tint = Color(0xFFFF8A65),
                        modifier = Modifier.height(16.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.sunset),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = FormatUtil.formatTime(sunset, timeFormat),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SunArcCanvas(
    dayFraction: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val arcHeight = height * 0.7f
        val baseY = height * 0.85f
        val padding = 16f

        // Horizon line
        drawLine(
            color = Color.White.copy(alpha = 0.2f),
            start = Offset(padding, baseY),
            end = Offset(width - padding, baseY),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f))
        )

        // Arc path
        val arcPath = Path()
        val arcWidth = width - padding * 2
        val steps = 50
        for (i in 0..steps) {
            val t = i.toFloat() / steps
            val x = padding + t * arcWidth
            val y = baseY - sin(t * PI.toFloat()) * arcHeight
            if (i == 0) arcPath.moveTo(x, y) else arcPath.lineTo(x, y)
        }

        drawPath(
            path = arcPath,
            color = Color(0xFFFFD54F).copy(alpha = 0.4f),
            style = Stroke(width = 2f, cap = StrokeCap.Round)
        )

        // Sun position
        val sunX = padding + dayFraction * arcWidth
        val sunY = baseY - sin(dayFraction * PI.toFloat()) * arcHeight

        if (dayFraction in 0.01f..0.99f) {
            drawCircle(
                color = Color(0xFFFFD54F),
                radius = 8f,
                center = Offset(sunX, sunY)
            )
            drawCircle(
                color = Color(0xFFFFD54F).copy(alpha = 0.3f),
                radius = 14f,
                center = Offset(sunX, sunY)
            )
        }
    }
}

private fun parseTime(isoTime: String): LocalDateTime? = try {
    LocalDateTime.parse(isoTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
} catch (_: Exception) {
    null
}
