package tech.nullexdev.atlasfly.core.designsystem.theme

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

private val LightColorScheme = lightColorScheme(
    primary = AtlasTeal,
    onPrimary = Color.White,
    primaryContainer = TealContainer,
    onPrimaryContainer = TealOnContainer,
    secondary = AtlasTealDeep,
    onSecondary = Color.White,
    secondaryContainer = PaperContainer,
    onSecondaryContainer = Ink,
    tertiary = AtlasTealDeep,
    onTertiary = Color.White,
    tertiaryContainer = TealContainer,
    onTertiaryContainer = TealOnContainer,
    error = StampRed,
    onError = Color.White,
    errorContainer = Color(0xFFF6D6D4),
    onErrorContainer = Color(0xFF410002),
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperField,
    onSurfaceVariant = InkMuted,
    outline = InkFaint,
    outlineVariant = Color(0xFFD4CBBC),
    surfaceContainerLowest = PaperLowest,
    surfaceContainerLow = Paper,
    surfaceContainer = PaperContainer,
    surfaceContainerHigh = PaperContainerHigh,
    surfaceContainerHighest = Color(0xFFDDD3C3),
    inverseSurface = Ink,
    inverseOnSurface = Paper,
    inversePrimary = NightTeal,
    surfaceTint = AtlasTeal,
    scrim = Color.Black,
)

private val DarkColorScheme = darkColorScheme(
    primary = NightTeal,
    onPrimary = Color(0xFF00363C),
    primaryContainer = AtlasTealDeep,
    onPrimaryContainer = Color(0xFFD2EEF0),
    secondary = NightMuted,
    onSecondary = Night,
    secondaryContainer = NightField,
    onSecondaryContainer = NightInk,
    tertiary = NightTeal,
    onTertiary = Color(0xFF00363C),
    tertiaryContainer = NightField,
    onTertiaryContainer = NightInk,
    error = NightStamp,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Night,
    onBackground = NightInk,
    surface = Night,
    onSurface = NightInk,
    surfaceVariant = NightField,
    onSurfaceVariant = NightMuted,
    outline = NightLine,
    outlineVariant = Color(0xFF3D3933),
    surfaceContainerLowest = NightLowest,
    surfaceContainerLow = Night,
    surfaceContainer = NightContainer,
    surfaceContainerHigh = NightField,
    surfaceContainerHighest = Color(0xFF35302A),
    inverseSurface = NightInk,
    inverseOnSurface = Night,
    inversePrimary = AtlasTeal,
    surfaceTint = NightTeal,
    scrim = Color.Black,
)

@Composable
fun AtlasFlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AtlasFlyTypography,
        shapes = AtlasFlyShapes,
        content = content,
    )
}
