package com.clearsky.weather.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
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

class SmallWeatherWidget : GlanceAppWidget(errorUiLayout = R.layout.widget_error_layout) {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetDataProvider.getWidgetData(context)

        provideContent {
            ClearSkyWidgetTheme {
                if (data != null) {
                    SmallWidgetContent(data)
                } else {
                    WidgetErrorContent("No weather data")
                }
            }
        }
    }
}

@Composable
private fun SmallWidgetContent(data: WidgetWeatherData) {
    val gradient = WeatherCodeUtil.getGradient(data.weatherCode, data.isDay)
    val bgColor = gradient.firstOrNull()?.let {
        ColorProvider(it)
    } ?: GlanceTheme.colors.primaryContainer

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = GlanceModifier.defaultWeight()
        ) {
            Text(
                text = "${data.temperature.roundToInt()}°",
                style = TextStyle(
                    color = ColorProvider(androidx.compose.ui.graphics.Color.White),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = data.locationName,
                style = TextStyle(
                    color = ColorProvider(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)),
                    fontSize = 12.sp
                ),
                maxLines = 1
            )
        }

        Spacer(modifier = GlanceModifier.width(4.dp))

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = WeatherCodeUtil.getDescription(data.weatherCode),
                style = TextStyle(
                    color = ColorProvider(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)),
                    fontSize = 10.sp
                ),
                maxLines = 1
            )
            Text(
                text = "H:${data.highTemp.roundToInt()}° L:${data.lowTemp.roundToInt()}°",
                style = TextStyle(
                    color = ColorProvider(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f)),
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
fun WidgetErrorContent(message: String) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.errorContainer)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ClearSky",
            style = TextStyle(
                color = GlanceTheme.colors.onErrorContainer,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = message,
            style = TextStyle(
                color = GlanceTheme.colors.onErrorContainer,
                fontSize = 11.sp
            )
        )
        Text(
            text = "Tap to open app",
            style = TextStyle(
                color = GlanceTheme.colors.onErrorContainer,
                fontSize = 10.sp
            )
        )
    }
}

class SmallWeatherWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SmallWeatherWidget()
}
