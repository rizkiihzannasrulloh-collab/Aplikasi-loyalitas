package com.example.ui.theme

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

private val DarkColorScheme =
  darkColorScheme(
    primary = Purple80,
    onPrimary = NasiPurpleDark,
    primaryContainer = NasiPurple,
    onPrimaryContainer = NasiPurpleContainer,
    secondary = Pink80,
    onSecondary = NasiOrangeDark,
    secondaryContainer = NasiOrangeDark,
    onSecondaryContainer = NasiOrangeLight,
    tertiary = NasiYellow,
    background = Color(0xFF1B1424),
    surface = Color(0xFF241C30),
    surfaceVariant = Color(0xFF352B44)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = NasiPurple,
    onPrimary = Color.White,
    primaryContainer = NasiPurpleContainer,
    onPrimaryContainer = NasiPurpleDark,
    secondary = NasiOrange,
    onSecondary = Color.White,
    secondaryContainer = NasiOrangeContainer,
    onSecondaryContainer = NasiOrangeDark,
    tertiary = NasiYellow,
    background = Color(0xFFF9F5F0),
    surface = Color.White,
    surfaceVariant = NasiCreamDark,
    onBackground = Color(0xFF261933),
    onSurface = Color(0xFF261933)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
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
