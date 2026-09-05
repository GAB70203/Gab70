package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = BentoDarkPrimary,
    onPrimary = BentoDarkOnPrimary,
    primaryContainer = BentoDarkPrimaryContainer,
    onPrimaryContainer = BentoDarkOnPrimaryContainer,
    secondary = BentoDarkSecondaryContainer,
    onSecondary = BentoDarkOnSecondaryContainer,
    secondaryContainer = BentoDarkTileActive,
    onSecondaryContainer = BentoDarkOnSecondaryContainer,
    tertiary = BentoDarkPrimary,
    onTertiary = BentoDarkOnPrimary,
    tertiaryContainer = BentoDarkPrimaryContainer,
    onTertiaryContainer = BentoDarkOnPrimaryContainer,
    background = BentoDarkBackground,
    onBackground = BentoDarkOnBackground,
    surface = BentoDarkSurface,
    onSurface = BentoDarkOnSurface,
    surfaceVariant = BentoDarkTile,
    onSurfaceVariant = BentoDarkTextMuted,
    outline = BentoDarkOutline,
    outlineVariant = BentoDarkTileActive
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BentoPrimary,
    onPrimary = BentoOnPrimary,
    primaryContainer = BentoPrimaryContainer,
    onPrimaryContainer = BentoOnPrimaryContainer,
    secondary = BentoSecondary,
    onSecondary = BentoOnSecondary,
    secondaryContainer = BentoSecondaryContainer,
    onSecondaryContainer = BentoOnSecondaryContainer,
    tertiary = BentoPrimary,
    onTertiary = BentoOnPrimary,
    tertiaryContainer = BentoPrimaryContainer,
    onTertiaryContainer = BentoOnPrimaryContainer,
    background = BentoBackground,
    onBackground = BentoOnBackground,
    surface = BentoSurface,
    onSurface = BentoOnSurface,
    surfaceVariant = BentoTile,
    onSurfaceVariant = BentoTextMuted,
    outline = BentoOutline,
    outlineVariant = BentoOutlineVariant
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color default false to enforce the Bento Grid design theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
