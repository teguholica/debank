package com.debank.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Teal600 = Color(0xFF00897B)
private val Teal700 = Color(0xFF00796B)
private val Teal800 = Color(0xFF00695C)
private val Teal100 = Color(0xFFB2DFDB)
private val Teal50 = Color(0xFFE0F2F1)

private val LightColorScheme = lightColorScheme(
    primary = Teal600,
    onPrimary = Color.White,
    primaryContainer = Teal100,
    onPrimaryContainer = Teal800,
    secondary = Color(0xFF546E7A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFD8DC),
    onSecondaryContainer = Color(0xFF263238),
    tertiary = Color(0xFF00897B),
    background = Color(0xFFF5F7FA),
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE8EDF2),
    onSurfaceVariant = Color(0xFF49454F),
    error = Color(0xFFE53935),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFFCAC4D0),
    surfaceTint = Teal600
)

@Composable
fun DeBankTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
