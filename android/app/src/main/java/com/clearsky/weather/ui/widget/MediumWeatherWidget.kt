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
import com.clearsky.weather.ui.util.FormatUtil
import com.clearsky.weather.ui.util.WeatherCodeUtil
import kotlin.math.roundToInt

class MediumWeatherWidget : GlanceAppWidget(errorUiLayout = R.layout.widget_error_layout) {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetDataProvider.getWidgetData(context)

        provideContent {
            ClearSkyWidgetTheme {
                if (data != null) {
                    MediumWidgetContent(data)
                } else {
                    WidgetErrorContent("No weather data")
                }
            }
        }
    }
}

@Composable
private fun MediumWidgetContent(data: WidgetWeatherData) {
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
        // Top: Location + weather description
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = data.locationName,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 13.sp,
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

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Current temp row
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "${data.temperature.roundToInt()}°",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Column {
                Text(
                    text = "H:${data.highTemp.roundToInt()}° L:${data.lowTemp.roundToInt()}°",
                    style = TextStyle(
                        color = ColorProvider(Color.White.copy(alpha = 0.8f)),
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = "Feels ${data.feelsLike.roundToInt()}°",
                    style = TextStyle(
                        color = ColorProvider(Color.White.copy(alpha = 0.7f)),
                        fontSize = 10.sp
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(6.dp).defaultWeight())

        // Hourly forecast strip (4 hours)
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            data.hourlyForecasts.take(4).forEachIndexed { index, hourly ->
                if (index > 0) {
                    Spacer(modifier = GlanceModifier.width(2.dp))
                }
                Column(
                    modifier = GlanceModifier.defaultWeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (index == 0) "Now" else FormatUtil.formatHour(hourly.time),
                        style = TextStyle(
                            color = ColorProvider(Color.White.copy(alpha = 0.7f)),
                            fontSize = 9.sp
                        )
                    )
                    Text(
                        text = "${hourly.temperature.roundToInt()}°",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    if (hourly.precipitationProbability > 0) {
                        Text(
                            text = "${hourly.precipitationProbability}%",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF90CAF9)),
                                fontSize = 8.sp
                            )
                        )
                    }
                }
            }
        }

        // Stale data indicator with refresh action
        if (data.isStale) {
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = "${data.ageMinutes}min ago · Tap to refresh",
                style = TextStyle(
                    color = ColorProvider(Color.White.copy(alpha = 0.5f)),
                    fontSize = 8.sp
                ),
                modifier = GlanceModifier.clickable(actionRunCallback<RefreshWidgetAction>())
            )
        }
    }
}

class MediumWeatherWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MediumWeatherWidget()
}
