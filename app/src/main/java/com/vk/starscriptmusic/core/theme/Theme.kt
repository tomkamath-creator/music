package com.vk.starscriptmusic.core.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Dynamic Chameleon Accent Color extracted from album art
val LocalChameleonAccent = compositionLocalOf { NeoViolet }
val LocalChameleonSecondary = compositionLocalOf { NeoCyan }

private val DarkColorScheme = darkColorScheme(
    primary = NeoViolet,
    onPrimary = Color.White,
    primaryContainer = CardSurfaceElevated,
    onPrimaryContainer = TextPrimary,
    secondary = NeoCyan,
    onSecondary = Color.Black,
    secondaryContainer = CardSurface,
    onSecondaryContainer = TextPrimary,
    tertiary = NeoMagenta,
    onTertiary = Color.White,
    background = OledBlack,
    onBackground = TextPrimary,
    surface = DeepSpaceCharcoal,
    onSurface = TextPrimary,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder
)

@Composable
fun StarscriptMusicTheme(
    dynamicAccent: Color = NeoViolet,
    dynamicSecondary: Color = NeoCyan,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = OledBlack.toArgb()
                window.navigationBarColor = OledBlack.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    CompositionLocalProvider(
        LocalChameleonAccent provides dynamicAccent,
        LocalChameleonSecondary provides dynamicSecondary
    ) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = StarscriptTypography,
            content = content
        )
    }
}
