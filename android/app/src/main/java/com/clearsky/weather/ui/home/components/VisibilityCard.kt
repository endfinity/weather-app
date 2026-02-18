package com.clearsky.weather.ui.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.clearsky.weather.R
import com.clearsky.weather.ui.util.FormatUtil

@Composable
fun VisibilityCard(
    visibilityMeters: Int,
    modifier: Modifier = Modifier
) {
    val visDesc = "Visibility ${FormatUtil.formatVisibility(visibilityMeters)}. " +
        visibilityDescription(visibilityMeters)

    WeatherCard(
        title = stringResource(R.string.visibility),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.clearAndSetSemantics { contentDescription = visDesc }
        ) {
            Text(
                text = FormatUtil.formatVisibility(visibilityMeters),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = visibilityDescription(visibilityMeters),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun visibilityDescription(meters: Int): String = when {
    meters < 1000 -> stringResource(R.string.visibility_very_poor)
    meters < 4000 -> stringResource(R.string.visibility_poor)
    meters < 10000 -> stringResource(R.string.visibility_moderate)
    meters < 20000 -> stringResource(R.string.visibility_good)
    else -> stringResource(R.string.visibility_excellent)
}
