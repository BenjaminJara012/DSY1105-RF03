package com.librompaz.app.ui.theme

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

// Colores de la marca de demostración (azul y verde del logo); se pueden cambiar si la escuela
// entrega su manual de marca. Los tres de la versión clara deben mantenerse iguales a
// res/values/colors.xml (logo e ícono de la app).
private val AzulLibro = Color(0xFF283593)
private val VerdeSecundario = Color(0xFF2E7D32)
private val NaranjoAlerta = Color(0xFFEF6C00)

private val LightColors = lightColorScheme(
    primary = AzulLibro,
    onPrimary = Color.White,
    secondary = VerdeSecundario,
    onSecondary = Color.White,
    tertiary = NaranjoAlerta,
    onTertiary = Color.White
)

// Versión oscura: los mismos tonos, más claros para mantener el contraste sobre fondo oscuro.
private val DarkColors = darkColorScheme(
    primary = Color(0xFF9FA8DA),
    onPrimary = Color(0xFF1A237E),
    secondary = Color(0xFF81C784),
    onSecondary = Color(0xFF00390F),
    tertiary = Color(0xFFFFB74D),
    onTertiary = Color(0xFF3E2700)
)

@Composable
fun LibroMpazTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.primary.toArgb()
            // Barra clara (tema oscuro): íconos oscuros; barra azul (tema claro): íconos blancos.
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
