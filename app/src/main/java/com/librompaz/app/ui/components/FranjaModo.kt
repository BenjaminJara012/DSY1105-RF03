package com.librompaz.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.librompaz.app.viewmodel.LibroMpazViewModel

/**
 * Franja inferior, visible en todas las pantallas, que dice con qué fuente de datos se está
 * trabajando: "Modo local" (solo la base de este equipo) o "Modo servidor". Así se nota qué se está
 * demostrando. Va al final de la pantalla y se encarga del margen de la barra de navegación.
 */
@Composable
fun FranjaModo(viewModel: LibroMpazViewModel, modifier: Modifier = Modifier) {
    val modoLocal by viewModel.modoLocal.collectAsState()
    val texto = if (modoLocal) "Modo local · datos de este equipo, sin servidor" else "Modo servidor"
    Surface(
        color = if (modoLocal) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer,
        contentColor = if (modoLocal) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Text(
            texto,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
