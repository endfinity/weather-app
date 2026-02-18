package com.clearsky.weather.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.clearsky.weather.MainActivity
import com.clearsky.weather.R
import com.clearsky.weather.domain.model.AqiCategory
import com.clearsky.weather.ui.util.FormatUtil
import com.clearsky.weather.ui.util.WeatherCodeUtil
import kotlin.math.roundToInt

class XLWeatherWidget : GlanceAppWidget(errorUiLayout = R.layout.widget_error_layout) {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetDataProvider.getWidgetData(context)

        provideContent {
            ClearSkyWidgetTheme {
                if (data != null) {
                    XLWidgetContent(data)
                } else {
                    WidgetErrorContent("No weather data")
                }
            }
        }
    }
}

@Composable
private fun XLWidgetContent(data: WidgetWeatherData) {
    val gradient = WeatherCodeUtil.getGradient(data.weatherCode, data.isDay)
    val bgColor = gradient.firstOrNull()?.let { ColorProvider(it) }
        ?: GlanceTheme.colors.primaryContainer

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        // Header
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = data.locationName,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text = WeatherCodeUtil.getDescription(data.weatherCode),
                style = TextStyle(
                    color = ColorProvider(Color.White.copy(alpha = 0.8f)),
                    fontSize = 11.sp
                ),
                maxLines = 1
            )
        }

        // Current temp + details
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(top = 4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "${data.temperature.roundToInt()}°",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Column {
                Text(
                    text = "H:${data.highTemp.roundToInt()}° L:${data.lowTemp.roundToInt()}°",
                    style = TextStyle(
                        color = ColorProvider(Color.White.copy(alpha = 0.8f)),
                        fontSize = 12.sp
                    )
                )
                Text(
                    text = "Feels like ${data.feelsLike.roundToInt()}°",
                    style = TextStyle(
                        color = ColorProvider(Color.White.copy(alpha = 0.7f)),
                        fontSize = 10.sp
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        // Stats row: Wind, Humidity, UV, AQI
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StatItem(label = "Wind", value = "${data.windSpeed.roundToInt()} km/h")
            Spacer(modifier = GlanceModifier.width(4.dp))
            StatItem(label = "Humidity", value = "${data.humidity}%")
            Spacer(modifier = GlanceModifier.width(4.dp))
            StatItem(label = "UV", value = FormatUtil.formatUvIndex(data.uvIndex))
            if (data.airQualityIndex != null) {
                Spacer(modifier = GlanceModifier.width(4.dp))
                StatItem(
                    label = "AQI",
                    value = "${data.airQualityIndex}",
                    subtitle = AqiCategory.fromAqi(data.airQualityIndex).label
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        // Divider
        Spacer(
            modifier = GlanceModifier.fillMaxWidth().height(1.dp)
                .background(ColorProvider(Color.White.copy(alpha = 0.2f)))
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Hourly forecast (8 hours)
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            data.hourlyForecasts.take(8).forEachIndexed { index, hourly ->
                if (index > 0) Spacer(modifier = GlanceModifier.width(1.dp))
                Column(
                    modifier = GlanceModifier.defaultWeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (index == 0) "Now" else FormatUtil.formatHour(hourly.time),
                        style = TextStyle(
                            color = ColorProvider(Color.White.copy(alpha = 0.7f)),
                            fontSize = 8.sp
                        )
                    )
                    Text(
                        text = "${hourly.temperature.roundToInt()}°",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    if (hourly.precipitationProbability > 0) {
                        Text(
                            text = "${hourly.precipitationProbability}%",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF90CAF9)),
                                fontSize = 7.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Divider
        Spacer(
            modifier = GlanceModifier.fillMaxWidth().height(1.dp)
                .background(ColorProvider(Color.White.copy(alpha = 0.2f)))
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        // 7-day forecast
        data.dailyForecasts.take(7).forEach { daily ->
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = FormatUtil.formatDayOfWeek(daily.date),
                    style = TextStyle(
                        color = ColorProvider(Color.White.copy(alpha = 0.9f)),
                        fontSize = 10.sp
                    ),
                    modifier = GlanceModifier.width(50.dp)
                )

                Text(
                    text = WeatherCodeUtil.getDescription(daily.weatherCode),
                    style = TextStyle(
                        color = ColorProvider(Color.White.copy(alpha = 0.6f)),
                        fontSize = 9.sp
                    ),
                    modifier = GlanceModifier.defaultWeight(),
                    maxLines = 1
                )

                if (daily.precipitationProbabilityMax > 0) {
                    Text(
                        text = "${daily.precipitationProbabilityMax}%",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF90CAF9)),
                            fontSize = 9.sp
                        ),
                        modifier = GlanceModifier.width(26.dp)
                    )
                } else {
                    Spacer(modifier = GlanceModifier.width(26.dp))
                }

                Text(
                    text = "${daily.temperatureMax.roundToInt()}°",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = GlanceModifier.width(24.dp)
                )
                Text(
                    text = "${daily.temperatureMin.roundToInt()}°",
                    style = TextStyle(
                        color = ColorProvider(Color.White.copy(alpha = 0.6f)),
                        fontSize = 10.sp
                    ),
                    modifier = GlanceModifier.width(24.dp)
                )
            }
        }

        // Stale indicator with refresh action
        if (data.isStale) {
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = "Updated ${data.ageMinutes}min ago · Tap to refresh",
                style = TextStyle(
                    color = ColorProvider(Color.White.copy(alpha = 0.5f)),
                    fontSize = 8.sp
                ),
                modifier = GlanceModifier.clickable(actionRunCallback<RefreshWidgetAction>())
            )
        }
    }
}

@Composable
private fun RowScope.StatItem(
    label: String,
    value: String,
    subtitle: String? = null
) {
    Column(
        modifier = GlanceModifier
            .defaultWeight()
            .background(ColorProvider(Color.White.copy(alpha = 0.1f)))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = ColorProvider(Color.White.copy(alpha = 0.6f)),
                fontSize = 8.sp
            )
        )
        Text(
            text = value,
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = TextStyle(
                    color = ColorProvider(Color.White.copy(alpha = 0.7f)),
                    fontSize = 7.sp
                )
            )
        }
    }
}

class XLWeatherWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = XLWeatherWidget()
}
