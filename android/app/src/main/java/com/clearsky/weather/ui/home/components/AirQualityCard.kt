package com.clearsky.weather.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.clearsky.weather.domain.model.AqiCategory
import com.clearsky.weather.domain.model.CurrentAirQuality

@Composable
fun AirQualityCard(
    airQuality: CurrentAirQuality,
    modifier: Modifier = Modifier
) {
    val aqiColor = aqiCategoryColor(airQuality.aqiCategory)
    val aqiDesc = "Air quality index ${airQuality.aqi}, ${airQuality.aqiCategory.label}. " +
        "PM2.5 ${airQuality.pm25}, PM10 ${airQuality.pm10}, " +
        "Ozone ${airQuality.ozone}, NO2 ${airQuality.nitrogenDioxide} micrograms per cubic meter. " +
        aqiAdvice(airQuality.aqiCategory)

    WeatherCard(
        title = stringResource(R.string.air_quality),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.clearAndSetSemantics { contentDescription = aqiDesc }
        ) {
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${airQuality.aqi}",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "- ${airQuality.aqiCategory.label}",
                    style = MaterialTheme.typography.titleMedium,
                    color = aqiColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            AqiScaleBar(aqi = airQuality.aqi)

            Spacer(modifier = Modifier.height(12.dp))

            AqiDetailRow("PM2.5", "${airQuality.pm25}", "µg/m")
            AqiDetailRow("PM10", "${airQuality.pm10}", "µg/m")
            AqiDetailRow("O", "${airQuality.ozone}", "µg/m")
            AqiDetailRow("NO", "${airQuality.nitrogenDioxide}", "µg/m")

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = aqiAdvice(airQuality.aqiCategory),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun AqiScaleBar(
    aqi: Int,
    modifier: Modifier = Modifier
) {
    val fraction = (aqi / 300f).coerceIn(0f, 1f)
    val colors = listOf(
        Color(0xFF4CAF50),
        Color(0xFFFFEB3B),
        Color(0xFFFF9800),
        Color(0xFFF44336),
        Color(0xFF9C27B0),
        Color(0xFF7B1FA2)
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
    ) {
        val barWidth = size.width
        val barHeight = size.height
        val segmentWidth = barWidth / colors.size

        colors.forEachIndexed { index, color ->
            drawRect(
                color = color,
                topLeft = Offset(index * segmentWidth, 0f),
                size = Size(segmentWidth, barHeight)
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
private fun AqiDetailRow(
    label: String,
    value: String,
    unit: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.width(48.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}

private fun aqiCategoryColor(category: AqiCategory): Color = when (category) {
    AqiCategory.GOOD -> Color(0xFF4CAF50)
    AqiCategory.MODERATE -> Color(0xFFFFEB3B)
    AqiCategory.UNHEALTHY_SENSITIVE -> Color(0xFFFF9800)
    AqiCategory.UNHEALTHY -> Color(0xFFF44336)
    AqiCategory.VERY_UNHEALTHY -> Color(0xFF9C27B0)
    AqiCategory.HAZARDOUS -> Color(0xFF7B1FA2)
}

@Composable
private fun aqiAdvice(category: AqiCategory): String = when (category) {
    AqiCategory.GOOD -> stringResource(R.string.aqi_good)
    AqiCategory.MODERATE -> stringResource(R.string.aqi_moderate)
    AqiCategory.UNHEALTHY_SENSITIVE -> stringResource(R.string.aqi_unhealthy_sensitive)
    AqiCategory.UNHEALTHY -> stringResource(R.string.aqi_unhealthy)
    AqiCategory.VERY_UNHEALTHY -> stringResource(R.string.aqi_very_unhealthy)
    AqiCategory.HAZARDOUS -> stringResource(R.string.aqi_hazardous)
}
