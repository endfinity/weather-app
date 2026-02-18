package com.clearsky.weather.ui.home.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.clearsky.weather.R
import com.clearsky.weather.domain.model.DailyForecast
import com.clearsky.weather.domain.model.TemperatureUnit
import com.clearsky.weather.ui.util.FormatUtil
import com.clearsky.weather.ui.util.WeatherCodeUtil
import com.clearsky.weather.ui.util.WeatherIcon
import kotlin.math.roundToInt

@Composable
fun DailyForecastCard(
    dailyForecasts: List<DailyForecast>,
    temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    modifier: Modifier = Modifier
) {
    val allMin = dailyForecasts.minOfOrNull { it.temperatureMin } ?: 0.0
    val allMax = dailyForecasts.maxOfOrNull { it.temperatureMax } ?: 100.0
    val tempRange = allMax - allMin

    WeatherCard(
        title = stringResource(R.string.daily_forecast),
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            dailyForecasts.take(10).forEach { forecast ->
                DailyRow(
                    forecast = forecast,
                    globalMin = allMin,
                    tempRange = tempRange,
                    temperatureUnit = temperatureUnit
                )
            }
        }
    }
}

@Composable
private fun DailyRow(
    forecast: DailyForecast,
    globalMin: Double,
    tempRange: Double,
    temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    modifier: Modifier = Modifier
) {
    val dayStr = FormatUtil.formatDayOfWeek(forecast.date)
    val weatherStr = WeatherCodeUtil.getDescription(forecast.weatherCode)
    val lowStr = FormatUtil.formatTemperatureValue(forecast.temperatureMin, temperatureUnit)
    val highStr = FormatUtil.formatTemperatureValue(forecast.temperatureMax, temperatureUnit)
    val precipStr = if (forecast.precipitationProbabilityMax > 0) {
        ", ${forecast.precipitationProbabilityMax}% precipitation"
    } else ""
    val rowDesc = "$dayStr: $weatherStr, low $lowStr, high $highStr$precipStr"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clearAndSetSemantics { contentDescription = rowDesc },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = FormatUtil.formatDayOfWeek(forecast.date),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            modifier = Modifier.width(56.dp)
        )

        WeatherIcon(
            weatherCode = forecast.weatherCode,
            isDay = true,
            modifier = Modifier.size(24.dp),
            tint = Color.White
        )

        Spacer(modifier = Modifier.width(8.dp))

        if (forecast.precipitationProbabilityMax > 0) {
            Text(
                text = "${forecast.precipitationProbabilityMax}%",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64B5F6),
                modifier = Modifier.width(32.dp),
                textAlign = TextAlign.End
            )
        } else {
            Spacer(modifier = Modifier.width(32.dp))
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = FormatUtil.formatTemperatureValue(forecast.temperatureMin, temperatureUnit),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End
        )

        Spacer(modifier = Modifier.width(8.dp))

        TemperatureBar(
            min = forecast.temperatureMin,
            max = forecast.temperatureMax,
            globalMin = globalMin,
            tempRange = tempRange,
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = FormatUtil.formatTemperatureValue(forecast.temperatureMax, temperatureUnit),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.Start
        )
    }
}

@Composable
private fun TemperatureBar(
    min: Double,
    max: Double,
    globalMin: Double,
    tempRange: Double,
    modifier: Modifier = Modifier
) {
    val startFraction = if (tempRange > 0) ((min - globalMin) / tempRange).toFloat().coerceIn(0f, 1f) else 0f
    val endFraction = if (tempRange > 0) ((max - globalMin) / tempRange).toFloat().coerceIn(0f, 1f) else 1f

    Row(modifier = modifier) {
        if (startFraction > 0.001f) {
            Spacer(modifier = Modifier.weight(startFraction))
        }

        Box(
            modifier = Modifier
                .weight((endFraction - startFraction).coerceAtLeast(0.01f))
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF64B5F6),
                            Color(0xFFFFF176),
                            Color(0xFFFF8A65)
                        )
                    )
                )
        )

        val remaining = 1f - endFraction
        if (remaining > 0.001f) {
            Spacer(modifier = Modifier.weight(remaining))
        }
    }
}
