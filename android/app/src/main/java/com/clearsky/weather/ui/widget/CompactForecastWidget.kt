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
import com.clearsky.weather.ui.util.WeatherCodeUtil
import kotlin.math.roundToInt

class CompactForecastWidget : GlanceAppWidget(errorUiLayout = R.layout.widget_error_layout) {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetDataProvider.getWidgetData(context)

        provideContent {
            ClearSkyWidgetTheme {
                if (data != null) {
                    CompactForecastContent(data)
                } else {
                    WidgetErrorContent("No forecast data")
                }
            }
        }
    }
}

@Composable
private fun CompactForecastContent(data: WidgetWeatherData) {
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
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${data.temperature.roundToInt()}°",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(GlanceModifier.width(8.dp))
            Column {
                Text(
                    text = data.locationName,
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
                Text(
                    text = WeatherCodeUtil.getDescription(data.weatherCode),
                    style = TextStyle(
                        color = ColorProvider(Color.White.copy(alpha = 0.8f)),
                        fontSize = 10.sp
                    ),
                    maxLines = 1
                )
            }
        }

        Spacer(GlanceModifier.height(8.dp))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            data.dailyForecasts.take(5).forEach { day ->
                DayColumn(day)
                Spacer(GlanceModifier.width(2.dp))
            }
        }
    }
}

@Composable
private fun DayColumn(day: WidgetDailyItem) {
    val dayLabel = try {
        val parts = day.date.split("-")
        val month = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val dayNum = parts.getOrNull(2)?.toIntOrNull() ?: 0
        "$month/$dayNum"
    } catch (_: Exception) {
        day.date.takeLast(5)
    }

    Column(
        modifier = GlanceModifier.defaultWeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dayLabel,
            style = TextStyle(
                color = ColorProvider(Color.White.copy(alpha = 0.7f)),
                fontSize = 9.sp
            )
        )
        Text(
            text = WeatherCodeUtil.getEmoji(day.weatherCode),
            style = TextStyle(fontSize = 14.sp)
        )
        Text(
            text = "${day.temperatureMax.roundToInt()}°",
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = "${day.temperatureMin.roundToInt()}°",
            style = TextStyle(
                color = ColorProvider(Color.White.copy(alpha = 0.6f)),
                fontSize = 9.sp
            )
        )
    }
}

class CompactForecastWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CompactForecastWidget()
}
