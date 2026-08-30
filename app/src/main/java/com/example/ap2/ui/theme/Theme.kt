package com.example.ap2.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.ap2.ui.theme.LightColorScheme

private val DarkColorScheme = darkColorScheme(
    primary = Color.DarkGray,                            //selbst gewähltes dark Gray
    secondary = Color.White,
    tertiary = Color.LightGray,
    background = Color.DarkGray.copy(alpha = 0.9f),      //selbst gewähltes dark Gray
    surface = Color.LightGray.copy(alpha = 0.07f),
    onPrimary = Color.Gray,
    onBackground = Color.White.copy(alpha = 0.08f),
    surfaceTint = Color.Transparent
)

private val LightColorScheme = lightColorScheme(
    primary = Color.White,
    secondary = Color.Black,
    tertiary = Color.Gray,
    background = Color.White.copy(alpha = 0.9f),
    surface = Color.Black.copy(alpha = 0.07f),
    onPrimary = Color.DarkGray,
    onBackground = Color.Black.copy(alpha = 0.08f),
    surfaceTint = Color.Transparent
)

@Composable
fun MoCo_2026Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}