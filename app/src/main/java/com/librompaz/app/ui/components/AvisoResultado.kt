package com.librompaz.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.librompaz.app.util.vibrar
import com.librompaz.app.viewmodel.Aviso
import com.librompaz.app.viewmodel.LibroMpazViewModel
import kotlinx.coroutines.delay

/**
 * Mensaje de resultado del ViewModel (éxito o error), animado con AnimatedVisibility: aparece con
 * un fundido y expansión, vibra una vez (recurso nativo: la vibración es más larga si es un error)
 * y se oculta solo a los 3 segundos.
 */
@Composable
fun AvisoDelSistema(viewModel: LibroMpazViewModel, modifier: Modifier = Modifier) {
    val aviso by viewModel.aviso.collectAsState()
    AvisoResultado(aviso = aviso, onOcultar = viewModel::descartarAviso, modifier = modifier)
}

@Composable
fun AvisoResultado(aviso: Aviso?, onOcultar: () -> Unit, modifier: Modifier = Modifier) {
    val contexto = LocalContext.current
    // Se recuerda el último aviso para que no desaparezca de golpe durante la animación de salida.
    var ultimo by remember { mutableStateOf<Aviso?>(null) }
    if (aviso != null) ultimo = aviso

    LaunchedEffect(aviso?.numero) {
        if (aviso != null) {
            vibrar(contexto, if (aviso.esError) 150L else 40L)
            delay(3_000)
            onOcultar()
        }
    }

    AnimatedVisibility(
        visible = aviso != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        val mostrado = ultimo
        if (mostrado != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (mostrado.esError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = if (mostrado.esError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        if (mostrado.esError) Icons.Filled.ErrorOutline else Icons.Filled.CheckCircle,
                        contentDescription = if (mostrado.esError) "Error" else "Listo"
                    )
                    Text(mostrado.texto, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
