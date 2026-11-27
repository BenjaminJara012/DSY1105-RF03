package com.librompaz.app.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Campo de texto estándar de la app (un solo lugar para el estilo de la validación por campo):
 *  - con error: borde rojo (isError), el mensaje debajo (supportingText) y un ícono de error;
 *  - sin error: puede mostrar un texto de [ayuda] (por ejemplo, un contador) y su [trailingIcon].
 *
 * @param error mensaje de validación (null = sin error).
 * @param icono ícono inicial que indica de qué es el campo.
 * @param iconoDeError false si el campo ya tiene su propio ícono final que no debe reemplazarse (por
 *   ejemplo, el ojo de mostrar/ocultar clave).
 */
@Composable
fun CampoTexto(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    ayuda: String? = null,
    icono: ImageVector? = null,
    marcador: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null,
    iconoDeError: Boolean = true
) {
    val iconoInicial: (@Composable () -> Unit)? = if (icono != null) {
        { Icon(icono, contentDescription = null) }
    } else {
        null
    }
    val iconoFinal: (@Composable () -> Unit)? = if (error != null && iconoDeError) {
        { Icon(Icons.Filled.ErrorOutline, contentDescription = "Error", tint = MaterialTheme.colorScheme.error) }
    } else {
        trailingIcon
    }
    val textoDeApoyo = error ?: ayuda
    val apoyo: (@Composable () -> Unit)? = if (textoDeApoyo != null) {
        { Text(textoDeApoyo) }
    } else {
        null
    }
    val textoMarcador: (@Composable () -> Unit)? = if (marcador != null) {
        { Text(marcador) }
    } else {
        null
    }

    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        modifier = modifier,
        label = { Text(etiqueta) },
        placeholder = textoMarcador,
        leadingIcon = iconoInicial,
        trailingIcon = iconoFinal,
        isError = error != null,
        supportingText = apoyo,
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation
    )
}
