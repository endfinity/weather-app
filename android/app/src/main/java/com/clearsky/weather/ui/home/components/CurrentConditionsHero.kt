package com.clearsky.weather.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.clearsky.weather.R
import com.clearsky.weather.domain.model.CurrentWeather
import com.clearsky.weather.domain.model.DailyForecast
import com.clearsky.weather.domain.model.TemperatureUnit
import com.clearsky.weather.ui.util.AnimatedWeatherIcon
import com.clearsky.weather.ui.util.FormatUtil
import com.clearsky.weather.ui.util.UnitConverter
import com.clearsky.weather.ui.util.WeatherIcon
import kotlin.math.roundToInt

@Composable
fun CurrentConditionsHero(
    current: CurrentWeather,
    todayForecast: DailyForecast?,
    locationName: String,
    temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    modifier: Modifier = Modifier
) {
    val tempStr = FormatUtil.formatTemperature(current.temperature, temperatureUnit)
    val highStr = todayForecast?.let { FormatUtil.formatTemperature(it.temperatureMax, temperatureUnit) }
    val lowStr = todayForecast?.let { FormatUtil.formatTemperature(it.temperatureMin, temperatureUnit) }
    val feelsStr = FormatUtil.formatTemperature(current.feelsLike, temperatureUnit)
    val hlDesc = if (highStr != null && lowStr != null) " High $highStr, Low $lowStr." else ""
    val heroDescription = "$locationName. $tempStr, ${current.weatherDescription}.$hlDesc Feels like $feelsStr."

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .clearAndSetSemantics { contentDescription = heroDescription },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = locationName,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(4.dp))

        val targetTemp = UnitConverter.convertTemperature(current.temperature, temperatureUnit)
        val animatedTemp by animateFloatAsState(
            targetValue = targetTemp.toFloat(),
            animationSpec = tween(durationMillis = 800),
            label = "temp_anim"
        )
        val symbol = temperatureUnit.symbol

        Text(
            text = "${animatedTemp.roundToInt()}°$symbol",
            color = Color.White,
            fontSize = 96.sp,
            fontWeight = FontWeight.Thin,
            lineHeight = 96.sp
        )

        WeatherIcon(
            weatherCode = current.weatherCode,
            isDay = current.isDay,
            modifier = Modifier.size(40.dp),
            tint = Color.White
        )

        AnimatedWeatherIcon(
            weatherCode = current.weatherCode,
            isDay = current.isDay,
            modifier = Modifier.size(48.dp),
            tint = Color.White.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = current.weatherDescription,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (todayForecast != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.high_format, FormatUtil.formatTemperature(todayForecast.temperatureMax, temperatureUnit)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(R.string.low_format, FormatUtil.formatTemperature(todayForecast.temperatureMin, temperatureUnit)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Text(
            text = stringResource(R.string.feels_like_format, FormatUtil.formatTemperature(current.feelsLike, temperatureUnit)),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}
