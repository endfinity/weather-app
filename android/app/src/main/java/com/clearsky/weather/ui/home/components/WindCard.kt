package com.clearsky.weather.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.clearsky.weather.R
import com.clearsky.weather.domain.model.WindSpeedUnit
import com.clearsky.weather.ui.util.FormatUtil
import com.clearsky.weather.ui.util.UnitConverter
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun WindCard(
    windSpeed: Double,
    windDirection: Int,
    windGusts: Double,
    windSpeedUnit: WindSpeedUnit = WindSpeedUnit.KMH,
    modifier: Modifier = Modifier
) {
    val windDesc = "Wind ${FormatUtil.formatWindSpeed(windSpeed, windSpeedUnit)}, " +
        "gusts ${FormatUtil.formatWindSpeed(windGusts, windSpeedUnit)}, " +
        "direction ${FormatUtil.formatWindDirection(windDirection)}"

    WeatherCard(
        title = stringResource(R.string.wind),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clearAndSetSemantics { contentDescription = windDesc },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = FormatUtil.formatWindSpeedValue(windSpeed, windSpeedUnit),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = windSpeedUnit.symbol,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.gusts_format, FormatUtil.formatWindSpeed(windGusts, windSpeedUnit)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = stringResource(R.string.direction_format, FormatUtil.formatWindDirection(windDirection)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            CompassRose(
                windDirection = windDirection,
                modifier = Modifier.size(100.dp)
            )
        }
    }
}

@Composable
private fun CompassRose(
    windDirection: Int,
    modifier: Modifier = Modifier
) {
    val directions = listOf(
        stringResource(R.string.compass_n),
        stringResource(R.string.compass_e),
        stringResource(R.string.compass_s),
        stringResource(R.string.compass_w)
    )
    val textColor = Color.White.copy(alpha = 0.5f)
    val arrowColor = Color.White

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(80.dp)) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.minDimension / 2 - 8f

            drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 2f)
            )

            for (i in 0 until 8) {
                val angle = Math.toRadians((i * 45.0) - 90.0)
                val tickLen = if (i % 2 == 0) 8f else 5f
                val outerX = centerX + (radius * cos(angle)).toFloat()
                val outerY = centerY + (radius * sin(angle)).toFloat()
                val innerX = centerX + ((radius - tickLen) * cos(angle)).toFloat()
                val innerY = centerY + ((radius - tickLen) * sin(angle)).toFloat()

                drawLine(
                    color = Color.White.copy(alpha = 0.4f),
                    start = Offset(innerX, innerY),
                    end = Offset(outerX, outerY),
                    strokeWidth = 2f
                )
            }

            val arrowAngle = Math.toRadians(windDirection.toDouble() - 90.0)
            val arrowLen = radius * 0.65f
            val arrowEndX = centerX + (arrowLen * cos(arrowAngle)).toFloat()
            val arrowEndY = centerY + (arrowLen * sin(arrowAngle)).toFloat()

            drawLine(
                color = arrowColor,
                start = Offset(centerX, centerY),
                end = Offset(arrowEndX, arrowEndY),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )

            drawCircle(
                color = arrowColor,
                radius = 4f,
                center = Offset(centerX, centerY)
            )
        }

        Text(
            text = "N",
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        Text(
            text = "S",
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        Text(
            text = "E",
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
        Text(
            text = "W",
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.align(Alignment.CenterStart)
        )
    }
}
