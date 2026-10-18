package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// InAir Palette (Strict Design System)
val InAirPrimary = Color(0xFFFF2D3F)
val InAirPrimaryContainer = Color(0xFF7A0012)
val InAirOnPrimary = Color(0xFFFFFFFF)
val InAirOnPrimaryContainer = Color(0xFFFFDAD9)

val InAirSecondary = Color(0xFFFF6B6B)
val InAirSecondaryContainer = Color(0xFF4A1016)
val InAirOnSecondary = Color(0xFF2B0007)
val InAirOnSecondaryContainer = Color(0xFFFFDAD9)

val InAirTertiary = Color(0xFFFF8577)
val InAirOnTertiary = Color(0xFF4C0008)
val InAirTertiaryContainer = Color(0xFF67000F)
val InAirOnTertiaryContainer = Color(0xFFFFDAD8)

val InAirBackground = Color(0xFF0D0507)
val InAirOnBackground = Color(0xFFFBEAEA)

val InAirSurface = Color(0xFF1A0A0D)
val InAirOnSurface = Color(0xFFFBEAEA)
val InAirSurfaceVariant = Color(0xFF2A1216)
val InAirOnSurfaceVariant = Color(0xFFD4A5AB)
val InAirOutline = Color(0x33FF2D3F)
val InAirOutlineVariant = Color(0x1AFF2D3F)

val InAirRedDark = Color(0xFFB3001B)
val InAirGlow = Color(0x66FF2D3F)
val InAirCardBorder = Color(0x26FF2D3F)

// Signature 135-degree red gradient
val InAirSignatureGradient = Brush.linearGradient(
    colors = listOf(InAirPrimary, InAirRedDark)
)

val InAirSurfaceGradient = Brush.verticalGradient(
    colors = listOf(InAirSurfaceVariant, InAirSurface)
)

val InAirRadialGlow = Brush.radialGradient(
    colors = listOf(Color(0x40FF2D3F), Color.Transparent)
)
