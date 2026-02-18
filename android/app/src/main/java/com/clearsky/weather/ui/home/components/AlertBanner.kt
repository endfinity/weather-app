package com.clearsky.weather.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clearsky.weather.R
import com.clearsky.weather.domain.model.AlertSeverity
import com.clearsky.weather.domain.model.WeatherAlert

@Composable
fun AlertBanner(
    alerts: List<WeatherAlert>,
    onAlertClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (alerts.isEmpty()) return

    val topAlert = alerts.maxByOrNull { it.severity.ordinal } ?: return

    val backgroundColor = when (topAlert.severity) {
        AlertSeverity.EXTREME -> Color(0xFFD32F2F)
        AlertSeverity.SEVERE -> Color(0xFFE65100)
        AlertSeverity.MODERATE -> Color(0xFFF57F17)
        AlertSeverity.MINOR -> Color(0xFF1565C0)
    }

    val textColor = Color.White

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor.copy(alpha = 0.95f))
            .clickable { onAlertClick(topAlert.id) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = stringResource(R.string.alert),
            tint = textColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = topAlert.title,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1
            )
            if (alerts.size > 1) {
                Text(
                    text = if (alerts.size > 2)
                        stringResource(R.string.more_alerts_plural_format, alerts.size - 1)
                    else
                        stringResource(R.string.more_alerts_format, alerts.size - 1),
                    color = textColor.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = stringResource(R.string.view_details),
            tint = textColor.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
    }
}
