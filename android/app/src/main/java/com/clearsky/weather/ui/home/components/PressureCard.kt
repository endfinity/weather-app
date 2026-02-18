package com.clearsky.weather.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.clearsky.weather.R
import com.clearsky.weather.ui.util.FormatUtil
import kotlin.math.roundToInt

@Composable
fun PressureCard(
    pressureMsl: Double,
    surfacePressure: Double,
    modifier: Modifier = Modifier
) {
    val trend = pressureTrend(pressureMsl)
    val pressureDesc = "Pressure ${FormatUtil.formatPressure(pressureMsl)}, $trend. " +
        "Surface pressure ${surfacePressure.roundToInt()} hPa."

    WeatherCard(
        title = stringResource(R.string.pressure),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clearAndSetSemantics { contentDescription = pressureDesc },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = FormatUtil.formatPressure(pressureMsl),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = trend,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.surface_format, surfacePressure.roundToInt()),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            PressureGauge(
                pressure = pressureMsl,
                modifier = Modifier.size(72.dp)
            )
        }
    }
}

@Composable
private fun PressureGauge(
    pressure: Double,
    modifier: Modifier = Modifier
) {
    // Normal range: 980-1050 hPa
    val fraction = ((pressure - 980.0) / 70.0).coerceIn(0.0, 1.0).toFloat()

    Canvas(modifier = modifier) {
        val strokeWidth = 8f
        val sweepAngle = 270f
        val startAngle = 135f

        drawArc(
            color = Color.White.copy(alpha = 0.15f),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            topLeft = Offset(strokeWidth, strokeWidth),
            size = Size(size.width - strokeWidth * 2, size.height - strokeWidth * 2)
        )

        drawArc(
            color = Color(0xFFCE93D8),
            startAngle = startAngle,
            sweepAngle = sweepAngle * fraction,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            topLeft = Offset(strokeWidth, strokeWidth),
            size = Size(size.width - strokeWidth * 2, size.height - strokeWidth * 2)
        )
    }
}

@Composable
private fun pressureTrend(pressure: Double): String = when {
    pressure < 1000 -> stringResource(R.string.pressure_low)
    pressure < 1013 -> stringResource(R.string.pressure_below_average)
    pressure < 1020 -> stringResource(R.string.pressure_normal)
    pressure < 1030 -> stringResource(R.string.pressure_above_average)
    else -> stringResource(R.string.pressure_high)
}
