package com.clearsky.weather.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.clearsky.weather.R

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isNetworkError = message.contains("internet", ignoreCase = true) ||
        message.contains("network", ignoreCase = true) ||
        message.contains("connect", ignoreCase = true) ||
        message.contains("host", ignoreCase = true) ||
        message.contains("timeout", ignoreCase = true)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (isNetworkError) Icons.Filled.WifiOff else Icons.Filled.CloudOff,
            contentDescription = if (isNetworkError) stringResource(R.string.no_internet_icon) else stringResource(R.string.error_loading_icon),
            modifier = Modifier.size(64.dp),
            tint = Color.White.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isNetworkError) stringResource(R.string.no_internet_title) else stringResource(R.string.unable_to_load),
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isNetworkError) {
                stringResource(R.string.no_internet_message)
            } else {
                message
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.2f),
                contentColor = Color.White
            )
        ) {
            Text(stringResource(R.string.try_again))
        }
    }
}
