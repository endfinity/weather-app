package com.clearsky.weather.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
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
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.clearsky.weather.MainActivity
import com.clearsky.weather.R

class AqiWidget : GlanceAppWidget(errorUiLayout = R.layout.widget_error_layout) {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetDataProvider.getWidgetData(context)

        provideContent {
            ClearSkyWidgetTheme {
                if (data != null) {
                    AqiWidgetContent(data)
                } else {
                    WidgetErrorContent("No data available")
                }
            }
        }
    }
}

private fun aqiColor(aqi: Int?): Color = when {
    aqi == null -> Color(0xFF9E9E9E)
    aqi <= 50 -> Color(0xFF4CAF50)
    aqi <= 100 -> Color(0xFFFFC107)
    aqi <= 150 -> Color(0xFFFF9800)
    aqi <= 200 -> Color(0xFFF44336)
    aqi <= 300 -> Color(0xFF9C27B0)
    else -> Color(0xFF7B1FA2)
}

private fun aqiLabel(aqi: Int?): String = when {
    aqi == null -> "N/A"
    aqi <= 50 -> "Good"
    aqi <= 100 -> "Moderate"
    aqi <= 150 -> "Unhealthy for Sensitive"
    aqi <= 200 -> "Unhealthy"
    aqi <= 300 -> "Very Unhealthy"
    else -> "Hazardous"
}

@Composable
private fun AqiWidgetContent(data: WidgetWeatherData) {
    val aqi = data.airQualityIndex
    val bgColor = aqiColor(aqi)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(bgColor))
            .padding(14.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = aqi?.toString() ?: "--",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(GlanceModifier.width(6.dp))
            Text(
                text = "AQI",
                style = TextStyle(
                    color = ColorProvider(Color.White.copy(alpha = 0.8f)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Spacer(GlanceModifier.height(4.dp))

        Text(
            text = aqiLabel(aqi),
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )

        Spacer(GlanceModifier.height(2.dp))

        Text(
            text = data.locationName,
            style = TextStyle(
                color = ColorProvider(Color.White.copy(alpha = 0.7f)),
                fontSize = 11.sp
            ),
            maxLines = 1
        )
    }
}

class AqiWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AqiWidget()
}
