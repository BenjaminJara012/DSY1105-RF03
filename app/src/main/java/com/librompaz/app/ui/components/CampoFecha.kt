package com.librompaz.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.librompaz.app.util.fechaDesdeMillis
import com.librompaz.app.util.hoy
import com.librompaz.app.util.millisDesdeFecha

/**
 * Campo de fecha de solo lectura que abre el calendario de Material 3 (DatePickerDialog).
 * Así el usuario no puede escribir una fecha con mal formato y el calendario no deja elegir días
 * futuros. Igual el ViewModel vuelve a validar la fecha antes de guardar.
 *
 * @param error mensaje de validación (null = sin error).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFecha(
    etiqueta: String,
    fecha: String,
    onFechaChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    var abierto by remember { mutableStateOf(false) }

    val apoyo: (@Composable () -> Unit)? = if (error != null) {
        { Text(error) }
    } else {
        null
    }

    OutlinedTextField(
        value = fecha,
        onValueChange = {},
        readOnly = true,
        label = { Text(etiqueta) },
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = { abierto = true }) {
                if (error != null) {
                    Icon(Icons.Filled.ErrorOutline, contentDescription = "Elegir fecha (hay un error)", tint = MaterialTheme.colorScheme.error)
                } else {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = "Elegir fecha")
                }
            }
        },
        isError = error != null,
        supportingText = apoyo,
        modifier = modifier
    )

    if (abierto) {
        val limite = millisDesdeFecha(hoy()) ?: Long.MAX_VALUE
        val estado = rememberDatePickerState(
            initialSelectedDateMillis = millisDesdeFecha(fecha),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= limite
            }
        )
        DatePickerDialog(
            onDismissRequest = { abierto = false },
            confirmButton = {
                TextButton(onClick = {
                    estado.selectedDateMillis?.let { onFechaChange(fechaDesdeMillis(it)) }
                    abierto = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { abierto = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = estado)
        }
    }
}
