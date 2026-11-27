package com.librompaz.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.librompaz.app.viewmodel.EstadoSincronizacion
import com.librompaz.app.viewmodel.LibroMpazViewModel

/**
 * Banner inferior con el estado de la conexión con el servidor, visible en todas las pantallas:
 *  - "Sincronizando": barra de progreso mientras se descargan los datos.
 *  - Sin conexión o error del servidor: mensaje en español, aviso de que se usan los datos de la
 *    última sincronización y botón "Reintentar".
 * Cuando todo está al día no se muestra nada.
 */
@Composable
fun BannerSincronizacion(viewModel: LibroMpazViewModel, modifier: Modifier = Modifier) {
    val estado by viewModel.estadoSync.collectAsState()
    // Se recuerda el último estado visible para que no cambie de golpe durante la animación de salida.
    var ultimo by remember { mutableStateOf<EstadoSincronizacion>(EstadoSincronizacion.AlDia) }
    if (estado != EstadoSincronizacion.AlDia) ultimo = estado

    AnimatedVisibility(
        visible = estado != EstadoSincronizacion.AlDia,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        val mostrado = ultimo
        val mensaje = when (mostrado) {
            is EstadoSincronizacion.SinConexion -> mostrado.mensaje
            is EstadoSincronizacion.Fallo -> mostrado.mensaje
            else -> null
        }
        val esError = mensaje != null
        Surface(
            color = if (esError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
            contentColor = if (esError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.fillMaxWidth()   // el margen de la barra de navegación lo pone FranjaModo
        ) {
            if (mensaje != null) {
                Row(
                    modifier = Modifier.padding(start = 12.dp, top = 6.dp, bottom = 6.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Warning, contentDescription = "Aviso")
                    Text(mensaje, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    TextButton(onClick = viewModel::sincronizar) { Text("Reintentar") }
                }
            } else {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Sincronizando con el servidor…", style = MaterialTheme.typography.bodySmall)
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
