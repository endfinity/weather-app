package com.clearsky.weather.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.clearsky.weather.R
import com.clearsky.weather.domain.model.TemperatureUnit
import com.clearsky.weather.ui.util.FormatUtil

@Composable
fun HumidityCard(
    humidity: Int,
    dewPoint: Double,
    temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    modifier: Modifier = Modifier
) {
    val humidityDesc = "Humidity $humidity percent. " +
        "Dew point ${FormatUtil.formatTemperature(dewPoint, temperatureUnit)}. " +
        humidityDescription(humidity)

    WeatherCard(
        title = stringResource(R.string.humidity),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clearAndSetSemantics { contentDescription = humidityDesc },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${humidity}%",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.dew_point_format, FormatUtil.formatTemperature(dewPoint, temperatureUnit)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = humidityDescription(humidity),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            HumidityGauge(
                percentage = humidity,
                modifier = Modifier.size(72.dp)
            )
        }
    }
}

@Composable
private fun HumidityGauge(
    percentage: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 8f
        val sweepAngle = 270f
        val startAngle = 135f
        val fraction = percentage / 100f

        drawArc(
            color = Color.White.copy(alpha = 0.15f),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            topLeft = Offset(strokeWidth, strokeWidth),
            size = Size(size.width - strokeWidth * 2, size.height - strokeWidth * 2)
        )

        drawArc(
            color = Color(0xFF64B5F6),
            startAngle = startAngle,
            sweepAngle = sweepAngle * fraction,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            topLeft = Offset(strokeWidth, strokeWidth),
            size = Size(size.width - strokeWidth * 2, size.height - strokeWidth * 2)
        )
    }
}

@Composable
private fun humidityDescription(humidity: Int): String = when {
    humidity < 30 -> stringResource(R.string.humidity_very_dry)
    humidity < 50 -> stringResource(R.string.humidity_comfortable)
    humidity < 70 -> stringResource(R.string.humidity_slightly_humid)
    else -> stringResource(R.string.humidity_very_humid)
}
