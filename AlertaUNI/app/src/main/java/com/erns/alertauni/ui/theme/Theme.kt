package com.erns.alertauni.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(

    /*    primary = Purple80,
        secondary = PurpleGrey80,
        tertiary = Pink80*/
    primary = MyPrimaryColor,
    secondary = MySecondaryColor,
    background = MyBackgroundColor,
    surface = MySurfaceColor,
)

private val LightColorScheme = lightColorScheme(
    /*    primary = Purple40,
        secondary = PurpleGrey40,
        tertiary = Pink40*/


    primary = MyPrimaryColor,
    onPrimary = MyPrimaryForegroundColor,
    primaryContainer = MySecondaryColor,
    onPrimaryContainer = MyPrimaryColor,
    secondary = MySecondaryColor,
    onSecondary = MyPrimaryColor,
    secondaryContainer = MySelectedColor,      // indicador de la NavigationBar y chips
    onSecondaryContainer = MyPrimaryColor,
    background = MyBackgroundColor,
    onBackground = MyForegroundColor,
    surface = MySurfaceColor,
    onSurface = MyForegroundColor,
    onSurfaceVariant = MyMutedForegroundColor,
    surfaceVariant = MyMutedColor,
    surfaceContainer = MySurfaceColor,         // fondo de NavigationBar
    surfaceContainerLow = MySurfaceColor,
    surfaceContainerHigh = MySurfaceColor,
    outline = MyMutedForegroundColor,
    outlineVariant = MyBorderColor,

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun AlertaUNITheme(
    // La app usa siempre la paleta de marca en modo claro: los tokens My*Color son fijos
    // y el color dinámico (Android 12+) pintaba FAB y barra con el fondo de pantalla.
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        //typography = Typography,
        typography = AppTypography,
        content = content
    )
}