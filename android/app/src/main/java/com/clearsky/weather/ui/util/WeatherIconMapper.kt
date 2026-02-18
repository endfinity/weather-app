package com.clearsky.weather.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object WeatherIconMapper {

    fun getIcon(weatherCode: Int, isDay: Boolean): ImageVector = when (weatherCode) {
        0 -> if (isDay) Icons.Filled.WbSunny else Icons.Filled.NightsStay
        1, 2 -> if (isDay) Icons.Filled.WbCloudy else Icons.Filled.NightsStay
        3 -> Icons.Filled.Cloud
        45, 48 -> Icons.Filled.Cloud
        51, 53, 55 -> Icons.Filled.Grain
        56, 57 -> Icons.Filled.AcUnit
        61, 63, 65 -> Icons.Filled.WaterDrop
        66, 67 -> Icons.Filled.AcUnit
        71, 73, 75, 77 -> Icons.Filled.AcUnit
        80, 81, 82 -> Icons.Filled.Umbrella
        85, 86 -> Icons.Filled.AcUnit
        95 -> Icons.Filled.Thunderstorm
        96, 99 -> Icons.Filled.FlashOn
        else -> Icons.Filled.WbSunny
    }
}

@Composable
fun WeatherIcon(
    weatherCode: Int,
    isDay: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    contentDescription: String? = null
) {
    Icon(
        imageVector = WeatherIconMapper.getIcon(weatherCode, isDay),
        contentDescription = contentDescription ?: WeatherCodeUtil.getDescription(weatherCode),
        modifier = modifier,
        tint = tint
    )
}
