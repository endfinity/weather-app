package com.clearsky.weather.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.clearsky.weather.R
import com.clearsky.weather.domain.model.PrecipitationUnit
import com.clearsky.weather.domain.model.TemperatureUnit
import com.clearsky.weather.domain.model.ThemeMode
import com.clearsky.weather.domain.model.TimeFormat
import com.clearsky.weather.domain.model.WindSpeedUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val prefs = uiState.preferences

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeader(stringResource(R.string.section_units))

            SettingsCard {
                SettingsOptionGroup(
                    title = stringResource(R.string.setting_temperature),
                    selectedValue = prefs.temperatureUnit.label,
                    options = TemperatureUnit.entries.map { it.label },
                    onOptionSelected = { label ->
                        TemperatureUnit.entries.find { it.label == label }
                            ?.let { viewModel.updateTemperatureUnit(it) }
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                SettingsOptionGroup(
                    title = stringResource(R.string.setting_wind_speed),
                    selectedValue = prefs.windSpeedUnit.label,
                    options = WindSpeedUnit.entries.map { it.label },
                    onOptionSelected = { label ->
                        WindSpeedUnit.entries.find { it.label == label }
                            ?.let { viewModel.updateWindSpeedUnit(it) }
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                SettingsOptionGroup(
                    title = stringResource(R.string.setting_precipitation),
                    selectedValue = prefs.precipitationUnit.label,
                    options = PrecipitationUnit.entries.map { it.label },
                    onOptionSelected = { label ->
                        PrecipitationUnit.entries.find { it.label == label }
                            ?.let { viewModel.updatePrecipitationUnit(it) }
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                SettingsOptionGroup(
                    title = stringResource(R.string.setting_time_format),
                    selectedValue = prefs.timeFormat.label,
                    options = TimeFormat.entries.map { it.label },
                    onOptionSelected = { label ->
                        TimeFormat.entries.find { it.label == label }
                            ?.let { viewModel.updateTimeFormat(it) }
                    }
                )
            }

            SectionHeader(stringResource(R.string.section_appearance))

            SettingsCard {
                SettingsOptionGroup(
                    title = stringResource(R.string.setting_theme),
                    selectedValue = prefs.themeMode.label,
                    options = ThemeMode.entries.map { it.label },
                    onOptionSelected = { label ->
                        ThemeMode.entries.find { it.label == label }
                            ?.let { viewModel.updateThemeMode(it) }
                    }
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    SettingsSwitchRow(
                        title = stringResource(R.string.setting_dynamic_color),
                        description = stringResource(R.string.setting_dynamic_color_desc),
                        checked = prefs.dynamicColor,
                        onCheckedChange = { viewModel.updateDynamicColor(it) }
                    )
                }
            }

            SectionHeader(stringResource(R.string.section_about))

            SettingsCard {
                SettingsInfoRow(label = stringResource(R.string.about_version), value = "1.0.0")
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingsInfoRow(label = stringResource(R.string.about_data_source), value = "Open-Meteo")
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingsInfoRow(label = stringResource(R.string.about_air_quality), value = "Open-Meteo AQI")
            }

            SettingsCard {
                val context = androidx.compose.ui.platform.LocalContext.current
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open-meteo.com/"))
                            context.startActivity(intent)
                        }
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.about_open_meteo_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.about_license),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(start = 4.dp)
    )
}

@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsOptionGroup(
    title: String,
    selectedValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = selectedValue,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            options.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onOptionSelected(option)
                            expanded = false
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = option == selectedValue,
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
