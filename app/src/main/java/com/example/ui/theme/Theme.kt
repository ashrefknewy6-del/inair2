package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// InAir is strictly dark-first
private val InAirDarkColorScheme = darkColorScheme(
    primary = InAirPrimary,
    onPrimary = InAirOnPrimary,
    primaryContainer = InAirPrimaryContainer,
    onPrimaryContainer = InAirOnPrimaryContainer,
    secondary = InAirSecondary,
    onSecondary = InAirOnSecondary,
    secondaryContainer = InAirSecondaryContainer,
    onSecondaryContainer = InAirOnSecondaryContainer,
    tertiary = InAirTertiary,
    onTertiary = InAirOnTertiary,
    tertiaryContainer = InAirTertiaryContainer,
    onTertiaryContainer = InAirOnTertiaryContainer,
    background = InAirBackground,
    onBackground = InAirOnBackground,
    surface = InAirSurface,
    onSurface = InAirOnSurface,
    surfaceVariant = InAirSurfaceVariant,
    onSurfaceVariant = InAirOnSurfaceVariant,
    outline = InAirOutline,
    outlineVariant = InAirOutlineVariant
)

val InAirShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun InAirTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = InAirDarkColorScheme,
        typography = InAirTypography,
        shapes = InAirShapes,
        content = content
    )
}
