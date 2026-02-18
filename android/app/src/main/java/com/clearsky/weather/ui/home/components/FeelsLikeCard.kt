package com.clearsky.weather.ui.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.clearsky.weather.R
import com.clearsky.weather.domain.model.TemperatureUnit
import com.clearsky.weather.ui.util.FormatUtil
import kotlin.math.abs

@Composable
fun FeelsLikeCard(
    feelsLike: Double,
    actual: Double,
    temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    modifier: Modifier = Modifier
) {
    val feelsDesc = "Feels like ${FormatUtil.formatTemperature(feelsLike, temperatureUnit)}. " +
        feelsLikeDescription(feelsLike, actual)

    WeatherCard(
        title = stringResource(R.string.feels_like),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.clearAndSetSemantics { contentDescription = feelsDesc }
        ) {
            Text(
                text = FormatUtil.formatTemperature(feelsLike, temperatureUnit),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = feelsLikeDescription(feelsLike, actual),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun feelsLikeDescription(feelsLike: Double, actual: Double): String {
    val diff = feelsLike - actual
    return when {
        abs(diff) < 1.5 -> stringResource(R.string.feels_like_similar)
        diff > 0 -> stringResource(R.string.feels_like_warmer)
        else -> stringResource(R.string.feels_like_cooler)
    }
}
