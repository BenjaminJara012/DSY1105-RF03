package com.librompaz.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.librompaz.app.viewmodel.LibroMpazViewModel

/**
 * Cuadro "Servidor", accesible desde el login: permite cambiar la dirección de la API sin
 * recompilar (por ejemplo, la IP del computador del aula) y probarla contra /health antes de
 * guardarla. La dirección se valida (campo con isError y mensaje): solo se aceptan servidores de la
 * red local, porque los datos no pueden salir del aula (RN-01, RNF-07).
 *
 * También tiene el interruptor "Modo local (sin servidor)": con él activo la app trabaja solo con la
 * base de este equipo (respaldo para cuando no hay servidor; para demostrar la integración se usa el
 * modo servidor). Con el modo local activo solo se muestra el interruptor (y "Restablecer datos
 * locales"): la dirección y "Probar conexión" aparecen al pasar a modo servidor. Si "Probar conexión"
 * falla se ofrece "Usar modo local". El modo local no abre ninguna conexión y no cambia la regla de que
 * las direcciones deben ser de la red local.
 */
@Composable
fun ServidorDialog(viewModel: LibroMpazViewModel, onCerrar: () -> Unit) {
    val estado by viewModel.servidorUi.collectAsState()
    val modoLocal by viewModel.modoLocal.collectAsState()
    var confirmarRestablecer by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCerrar,
        icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
        title = { Text("Servidor") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Modo local (sin servidor)", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Usa solo los datos de este equipo, con las mismas reglas de la app. Es un respaldo si no hay " +
                                "servidor. Lo que crees aquí no se envía al servidor ni se mezcla con sus datos, y al volver " +
                                "al modo servidor no se sincroniza.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(checked = modoLocal, onCheckedChange = viewModel::cambiarModoLocal)
                }
                if (!modoLocal) {
                    Text(
                        "Dirección del servidor de la red del aula. En el emulador es http://10.0.2.2:3000; " +
                            "en un teléfono, la IP del computador que ejecuta el servidor.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    CampoTexto(
                        valor = estado.url,
                        onValorChange = viewModel::onUrlServidorChange,
                        etiqueta = "Dirección del servidor",
                        marcador = "http://192.168.1.10:3000",
                        error = estado.error,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { viewModel.probarConexion() }),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = viewModel::probarConexion,
                        enabled = !estado.probando,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (estado.probando) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Probar conexión")
                        }
                    }
                }
                // El resultado se ve en los dos modos (prueba de conexión, cambio de modo, datos restablecidos).
                val resultado = estado.resultado
                if (resultado != null) {
                    val color = if (estado.resultadoEsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            if (estado.resultadoEsError) Icons.Filled.ErrorOutline else Icons.Filled.CheckCircle,
                            contentDescription = if (estado.resultadoEsError) "Error" else "Listo",
                            tint = color
                        )
                        Text(resultado, color = color, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (!modoLocal) {
                    if (estado.resultadoEsError) {
                        OutlinedButton(onClick = viewModel::usarModoLocal, modifier = Modifier.fillMaxWidth()) {
                            Text("Usar modo local")
                        }
                    }
                    TextButton(onClick = viewModel::restablecerServidor) { Text("Restablecer (emulador)") }
                }
                TextButton(onClick = { confirmarRestablecer = true }) { Text("Restablecer datos locales") }
            }
        },
        confirmButton = {
            if (modoLocal) {
                // En modo local no hay dirección que guardar.
                TextButton(onClick = onCerrar) { Text("Cerrar") }
            } else {
                TextButton(onClick = { if (viewModel.guardarServidor()) onCerrar() }) { Text("Guardar") }
            }
        },
        dismissButton = { if (!modoLocal) TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )

    if (confirmarRestablecer) {
        AlertDialog(
            onDismissRequest = { confirmarRestablecer = false },
            title = { Text("Restablecer datos locales") },
            text = {
                Text(
                    "Se borra lo creado en modo local y se vuelven a cargar los datos de ejemplo. " +
                        "Los datos del servidor no se tocan."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmarRestablecer = false
                    viewModel.restablecerDatosLocales()
                }) { Text("Restablecer") }
            },
            dismissButton = { TextButton(onClick = { confirmarRestablecer = false }) { Text("Cancelar") } }
        )
    }
}
