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

// Colores PROVISIONALES (no se cuenta con un manual de marca de la organización).
// TODO(equipo): reemplazar por los colores oficiales si la escuela o el CITT los entregan,
// y mantenerlos iguales a res/values/colors.xml (logo e ícono de la app).
private val AzulLibro = Color(0xFF283593)
private val VerdeSecundario = Color(0xFF2E7D32)
private val NaranjoAlerta = Color(0xFFEF6C00)

private val LightColors = lightColorScheme(
    primary = AzulLibro,
    secondary = VerdeSecundario,
    tertiary = NaranjoAlerta
)

private val DarkColors = darkColorScheme(
    primary = AzulLibro,
    secondary = VerdeSecundario,
    tertiary = NaranjoAlerta
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
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
