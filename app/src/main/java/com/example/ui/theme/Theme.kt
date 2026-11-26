package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val BharatTubeDarkColorScheme = darkColorScheme(
    primary = SaffronFlame,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF451B0B),
    onPrimaryContainer = Color(0xFFFFD8C8),
    secondary = LotusCoral,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4A1021),
    onSecondaryContainer = Color(0xFFFFD9E1),
    tertiary = ElectricCobalt,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceMuted,
    surfaceContainerHigh = DarkSurfaceElevated,
    outline = DarkOutline,
    error = Color(0xFFEF4444),
    onError = Color.White
)

private val BharatTubeLightColorScheme = lightColorScheme(
    primary = SaffronFlame,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE4D6),
    onPrimaryContainer = Color(0xFF471600),
    secondary = LotusCoral,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD9E2),
    onSecondaryContainer = Color(0xFF3E0014),
    tertiary = RoyalIndigo,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceMuted,
    surfaceContainerHigh = LightSurfaceElevated,
    outline = LightOutline,
    error = Color(0xFFDC2626),
    onError = Color.White
)

val BharatTubeShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun BharatTubeTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) BharatTubeDarkColorScheme else BharatTubeLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = BharatTubeTypography,
        shapes = BharatTubeShapes,
        content = content
    )
}
