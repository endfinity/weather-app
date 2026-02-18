package com.clearsky.weather.ui.pollen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollenDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: PollenDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pollen Forecast") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.loadData() }) {
                            Text("Retry")
                        }
                    }
                }
                !uiState.pollenAvailable -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Pollen data not available",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Pollen forecast is currently only available in Europe",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    PollenContent(uiState = uiState)
                }
            }
        }
    }
}

@Composable
private fun PollenContent(uiState: PollenUiState) {
    val summary = uiState.currentPollenSummary

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = "Current Pollen Levels",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (uiState.locationName.isNotEmpty()) {
                Text(
                    text = uiState.locationName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (summary != null) {
            item {
                PollenTypeCard(
                    name = "Alder",
                    level = summary.alderLevel,
                    icon = Icons.Default.Park,
                    color = Color(0xFF8BC34A)
                )
            }
            item {
                PollenTypeCard(
                    name = "Birch",
                    level = summary.birchLevel,
                    icon = Icons.Default.Park,
                    color = Color(0xFF4CAF50)
                )
            }
            item {
                PollenTypeCard(
                    name = "Grass",
                    level = summary.grassLevel,
                    icon = Icons.Default.Grass,
                    color = Color(0xFF66BB6A)
                )
            }
            item {
                PollenTypeCard(
                    name = "Mugwort",
                    level = summary.mugwortLevel,
                    icon = Icons.Default.Spa,
                    color = Color(0xFFFF9800)
                )
            }
            item {
                PollenTypeCard(
                    name = "Olive",
                    level = summary.oliveLevel,
                    icon = Icons.Default.Park,
                    color = Color(0xFF795548)
                )
            }
            item {
                PollenTypeCard(
                    name = "Ragweed",
                    level = summary.ragweedLevel,
                    icon = Icons.Default.Spa,
                    color = Color(0xFFF44336)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pollen data provided by Open-Meteo (Europe only)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PollenTypeCard(
    name: String,
    level: PollenLevel,
    icon: ImageVector,
    color: Color
) {
    val progress = when (level) {
        PollenLevel.NONE -> 0f
        PollenLevel.LOW -> 0.25f
        PollenLevel.MODERATE -> 0.5f
        PollenLevel.HIGH -> 0.75f
        PollenLevel.VERY_HIGH -> 1f
    }

    val levelColor = when (level) {
        PollenLevel.NONE -> MaterialTheme.colorScheme.outline
        PollenLevel.LOW -> Color(0xFF4CAF50)
        PollenLevel.MODERATE -> Color(0xFFFF9800)
        PollenLevel.HIGH -> Color(0xFFF44336)
        PollenLevel.VERY_HIGH -> Color(0xFF9C27B0)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = name,
                    modifier = Modifier.size(24.dp),
                    tint = color
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = level.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = levelColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(MaterialTheme.shapes.small),
                    color = levelColor,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            }
        }
    }
}
