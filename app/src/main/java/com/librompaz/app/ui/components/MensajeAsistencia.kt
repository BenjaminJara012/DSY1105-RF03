package com.librompaz.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Define los tipos de mensajes que puede mostrar
 * el módulo de asistencia.
 */
enum class TipoMensajeAsistencia {
    INFORMATIVO,
    EXITO,
    ERROR
}

/**
 * Componente reutilizable para mostrar mensajes.
 *
 * Cambia los colores según el tipo de mensaje,
 * manteniendo el diseño Material 3.
 *
 * @param texto Mensaje que verá el usuario.
 * @param tipo Tipo de mensaje que se mostrará.
 */
@Composable
fun MensajeAsistencia(
    texto: String,
    tipo: TipoMensajeAsistencia,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme

    // Determina el color de fondo del mensaje.
    val fondo = when (tipo) {
        TipoMensajeAsistencia.ERROR -> colores.errorContainer
        TipoMensajeAsistencia.EXITO -> colores.primaryContainer
        TipoMensajeAsistencia.INFORMATIVO -> colores.surfaceVariant
    }

    // Selecciona un color de texto con buen contraste.
    val colorTexto = when (tipo) {
        TipoMensajeAsistencia.ERROR -> colores.onErrorContainer
        TipoMensajeAsistencia.EXITO -> colores.onPrimaryContainer
        TipoMensajeAsistencia.INFORMATIVO -> colores.onSurfaceVariant
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = fondo,
        contentColor = colorTexto,
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}