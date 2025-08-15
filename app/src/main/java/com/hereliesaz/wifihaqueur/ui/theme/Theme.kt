package com.hereliesaz.wifihaqueur.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Import colors from the Color.kt file


private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    secondary = SecondaryDark,
    background = Color.Black, // Changed background color for DarkColorScheme back to Black
    surface = Color.Black,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    background = Color(0xFFF2F2F7) // LightColorScheme background remains light grey
)

@Composable
fun WifiHaqueurTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb() // Status bar color
            window.navigationBarColor = colorScheme.background.toArgb() // Navigation bar color
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars =
                darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
