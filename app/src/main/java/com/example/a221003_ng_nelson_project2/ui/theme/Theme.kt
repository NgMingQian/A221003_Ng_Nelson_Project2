package com.example.a221003_ng_nelson_project2.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = primaryLight,
    secondary = secondaryLight,
    tertiary = tertiaryLight,

    background = backgroundLight,
    surface = surfaceLight,
    error = errorLight,

    onBackground = onBackgroundLight,
    onSurface = onSurfaceLight,
    onSecondary = onSecondaryLight
)

private val DarkColorScheme = darkColorScheme(
    primary = primaryDark,
    secondary = secondaryDark,
    tertiary = tertiaryDark,

    background = backgroundDark,
    surface = surfaceDark,
    error = errorDark,

    onBackground = onBackgroundDark,
    onSurface = onSurfaceDark,
    onSecondary = onSecondaryDark
)

@Composable
fun A221003_Ng_Nelson_Project2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme =
        if (darkTheme) DarkColorScheme
        else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}