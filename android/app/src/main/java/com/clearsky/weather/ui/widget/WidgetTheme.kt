package com.clearsky.weather.ui.widget

import android.os.Build
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.glance.GlanceTheme
import androidx.glance.material3.ColorProviders
import com.clearsky.weather.ui.theme.*

private val WidgetLightColors = lightColorScheme(
    primary = SkyBlue40,
    onPrimary = Neutral99,
    primaryContainer = SkyBlue90,
    onPrimaryContainer = SkyBlue10,
    secondary = Amber40,
    onSecondary = Neutral99,
    secondaryContainer = Amber90,
    onSecondaryContainer = Amber10,
    tertiary = Green40,
    onTertiary = Neutral99,
    tertiaryContainer = Green90,
    onTertiaryContainer = Green10,
    error = Red40,
    onError = Neutral99,
    errorContainer = Red90,
    onErrorContainer = Red10,
    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = NeutralVariant90,
    onSurfaceVariant = NeutralVariant30
)

private val WidgetDarkColors = darkColorScheme(
    primary = SkyBlue80,
    onPrimary = SkyBlue20,
    primaryContainer = SkyBlue30,
    onPrimaryContainer = SkyBlue90,
    secondary = Amber80,
    onSecondary = Amber20,
    secondaryContainer = Amber30,
    onSecondaryContainer = Amber90,
    tertiary = Green80,
    onTertiary = Green20,
    tertiaryContainer = Green30,
    onTertiaryContainer = Green90,
    error = Red80,
    onError = Red20,
    errorContainer = Red30,
    onErrorContainer = Red90,
    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = NeutralVariant30,
    onSurfaceVariant = NeutralVariant80
)

val ClearSkyWidgetColorProviders = ColorProviders(
    light = WidgetLightColors,
    dark = WidgetDarkColors
)

@Composable
fun ClearSkyWidgetTheme(content: @Composable () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // On Android 12+, GlanceTheme without explicit colors uses dynamic Material You
        GlanceTheme(content = content)
    } else {
        GlanceTheme(colors = ClearSkyWidgetColorProviders, content = content)
    }
}
