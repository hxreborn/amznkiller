package eu.hxreborn.amznkiller.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Dracula palette
val DraculaBackground = Color(0xFF282A36)
val DraculaCurrentLine = Color(0xFF44475A)
val DraculaForeground = Color(0xFFF8F8F2)
val DraculaComment = Color(0xFF6272A4)
val DraculaCyan = Color(0xFF8BE9FD)
val DraculaGreen = Color(0xFF50FA7B)
val DraculaOrange = Color(0xFFFFB86C)
val DraculaPink = Color(0xFFFF79C6)
val DraculaPurple = Color(0xFFBD93F9)
val DraculaRed = Color(0xFFFF5555)
val DraculaYellow = Color(0xFFF1FA8C)

val LightColorScheme =
    lightColorScheme(
        primary = DraculaPurple,
        onPrimary = Color.White,
        primaryContainer = DraculaPurple.copy(alpha = 0.12f),
        onPrimaryContainer = DraculaPurple,
        secondary = DraculaPink,
        onSecondary = Color.White,
        secondaryContainer = DraculaPink.copy(alpha = 0.12f),
        onSecondaryContainer = DraculaPink,
        tertiary = DraculaCyan,
        onTertiary = DraculaBackground,
        tertiaryContainer = DraculaCyan.copy(alpha = 0.12f),
        onTertiaryContainer = DraculaCyan,
        background = DraculaForeground,
        onBackground = DraculaBackground,
        surface = Color.White,
        onSurface = DraculaBackground,
        surfaceVariant = Color(0xFFE8E8E8),
        onSurfaceVariant = DraculaComment,
        error = DraculaRed,
        onError = Color.White,
        outline = DraculaComment,
    )

val DarkColorScheme =
    darkColorScheme(
        primary = DraculaPurple,
        onPrimary = DraculaBackground,
        primaryContainer = DraculaCurrentLine,
        onPrimaryContainer = DraculaForeground,
        secondary = DraculaPink,
        onSecondary = DraculaBackground,
        secondaryContainer = DraculaCurrentLine,
        onSecondaryContainer = DraculaForeground,
        tertiary = DraculaCyan,
        onTertiary = DraculaBackground,
        tertiaryContainer = DraculaCurrentLine,
        onTertiaryContainer = DraculaForeground,
        background = DraculaBackground,
        onBackground = DraculaForeground,
        surface = DraculaBackground,
        onSurface = DraculaForeground,
        surfaceVariant = DraculaCurrentLine,
        onSurfaceVariant = DraculaComment,
        error = DraculaRed,
        onError = DraculaForeground,
        outline = DraculaComment,
    )
