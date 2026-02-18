package com.clearsky.weather.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.clearsky.weather.R
import com.clearsky.weather.domain.model.HourlyForecast
import com.clearsky.weather.domain.model.TemperatureUnit
import com.clearsky.weather.domain.model.TimeFormat
import com.clearsky.weather.ui.util.FormatUtil
import com.clearsky.weather.ui.util.WeatherCodeUtil
import com.clearsky.weather.ui.util.WeatherIcon

@Composable
fun HourlyForecastStrip(
    hourlyForecasts: List<HourlyForecast>,
    temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    timeFormat: TimeFormat = TimeFormat.H12,
    modifier: Modifier = Modifier
) {
    WeatherCard(
        title = stringResource(R.string.hourly_forecast),
        modifier = modifier
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(hourlyForecasts.take(24)) { forecast ->
                HourlyItem(
                    forecast = forecast,
                    temperatureUnit = temperatureUnit,
                    timeFormat = timeFormat
                )
            }
        }
    }
}

@Composable
private fun HourlyItem(
    forecast: HourlyForecast,
    temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    timeFormat: TimeFormat = TimeFormat.H12,
    modifier: Modifier = Modifier
) {
    val hourStr = FormatUtil.formatHour(forecast.time, timeFormat)
    val tempStr = FormatUtil.formatTemperature(forecast.temperature, temperatureUnit)
    val precipStr = if (forecast.precipitationProbability > 0) {
        ", ${forecast.precipitationProbability}% precipitation"
    } else ""
    val itemDesc = "$hourStr: $tempStr, ${WeatherCodeUtil.getDescription(forecast.weatherCode)}$precipStr"

    Column(
        modifier = modifier
            .width(56.dp)
            .padding(vertical = 8.dp)
            .clearAndSetSemantics { contentDescription = itemDesc },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = FormatUtil.formatHour(forecast.time, timeFormat),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f)
        )

        WeatherIcon(
            weatherCode = forecast.weatherCode,
            isDay = forecast.isDay,
            modifier = Modifier.size(24.dp),
            tint = Color.White
        )

        if (forecast.precipitationProbability > 0) {
            Text(
                text = "${forecast.precipitationProbability}%",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64B5F6),
                fontWeight = FontWeight.Medium
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = FormatUtil.formatTemperature(forecast.temperature, temperatureUnit),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}
