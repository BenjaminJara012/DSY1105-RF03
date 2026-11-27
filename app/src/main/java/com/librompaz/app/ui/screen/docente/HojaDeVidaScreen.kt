package com.librompaz.app.ui.screen.docente

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.librompaz.app.domain.ErroresAnotacion
import com.librompaz.app.model.Estudiante
import com.librompaz.app.model.TipoAnotacion
import com.librompaz.app.ui.components.AvisoDelSistema
import com.librompaz.app.ui.components.BarraSuperior
import com.librompaz.app.ui.components.CampoTexto
import com.librompaz.app.ui.components.SelectorDesplegable
import com.librompaz.app.viewmodel.LibroMpazViewModel

/**
 * Módulo "Hoja de vida" (docente, RF-10): registrar anotaciones positivas, negativas u
 * observaciones conductuales de un estudiante y ver su historial. Cada anotación queda con marca
 * temporal y firma del docente responsable (usuario + fecha + hora, puestos por el sistema) después
 * de una confirmación explícita. Una vez registrada no se puede editar ni eliminar (RN-06): esta
 * pantalla no ofrece esas acciones.
 */
@Composable
fun HojaDeVidaScreen(navController: NavHostController, viewModel: LibroMpazViewModel) {
    val cursoId by viewModel.cursoSeleccionadoId.collectAsState()
    val usuario by viewModel.usuarioActivo.collectAsState()
    val todas by viewModel.anotaciones.collectAsState()
    val todos by viewModel.estudiantes.collectAsState()
    val guardando by viewModel.guardando.collectAsState()
    val estudiantes = remember(todos, cursoId) { viewModel.estudiantesDelCurso(cursoId) }

    var estudiante by remember { mutableStateOf<Estudiante?>(null) }
    var tipo by remember { mutableStateOf<TipoAnotacion?>(null) }
    var descripcion by remember { mutableStateOf("") }
    var errores by remember { mutableStateOf(ErroresAnotacion()) }
    var confirmando by remember { mutableStateOf(false) }
    val historial = remember(todas, estudiante) { viewModel.anotacionesDe(estudiante?.id) }

    Scaffold(topBar = { BarraSuperior("Hoja de vida", navController) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SelectorDesplegable(
                etiqueta = "Estudiante",
                opciones = estudiantes,
                seleccion = estudiante,
                textoDe = { it.nombreFicticio },
                onSeleccion = {
                    estudiante = it
                    errores = errores.copy(estudiante = null)
                },
                textoVacio = "Elige un estudiante",
                icono = Icons.Filled.Person,
                error = errores.estudiante
            )
            SelectorDesplegable(
                etiqueta = "Tipo de anotación",
                opciones = TipoAnotacion.entries,
                seleccion = tipo,
                textoDe = { it.etiqueta },
                onSeleccion = {
                    tipo = it
                    errores = errores.copy(tipo = null)
                },
                icono = Icons.Filled.Info,
                error = errores.tipo
            )
            CampoTexto(
                valor = descripcion,
                onValorChange = {
                    descripcion = it
                    errores = errores.copy(descripcion = null)
                },
                etiqueta = "Descripción",
                icono = Icons.Filled.Edit,
                singleLine = false,
                minLines = 3,
                error = errores.descripcion,
                ayuda = "${descripcion.trim().length}/300 · mínimo 10 caracteres",
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Firma: ${usuario?.nombre.orEmpty()} · la fecha y hora se registran automáticamente.",
                style = MaterialTheme.typography.bodySmall
            )
            // Con la descripción vacía el botón está deshabilitado; el resto se valida al tocarlo.
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = descripcion.isNotBlank() && !guardando,
                onClick = {
                    errores = viewModel.validarNuevaAnotacion(estudiante, tipo, descripcion)
                    if (!errores.hayErrores) confirmando = true
                }
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Registrar anotación")
                }
            }

            AvisoDelSistema(viewModel)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                if (estudiante == null) "Elige un estudiante para ver su historial"
                else "Historial de ${estudiante?.nombreFicticio} (${historial.size})",
                style = MaterialTheme.typography.titleSmall
            )
            historial.forEach { anotacion ->
                val color = when (anotacion.tipo) {
                    TipoAnotacion.POSITIVA -> MaterialTheme.colorScheme.secondaryContainer
                    TipoAnotacion.NEGATIVA -> MaterialTheme.colorScheme.errorContainer
                    TipoAnotacion.OBSERVACION -> MaterialTheme.colorScheme.surfaceVariant
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = color),
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(anotacion.tipo.etiqueta, style = MaterialTheme.typography.titleSmall)
                        Text(anotacion.descripcion, style = MaterialTheme.typography.bodySmall)
                        Text("${anotacion.fechaHora} · Firmó: ${anotacion.firmadoPor}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }

    if (confirmando) {
        AlertDialog(
            onDismissRequest = { confirmando = false },
            title = { Text("¿Registrar anotación?") },
            text = { Text("Quedará firmada con tu nombre, la fecha y la hora, y no se podrá modificar.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmando = false
                    val elegido = estudiante
                    val tipoElegido = tipo
                    if (elegido != null && tipoElegido != null) {
                        viewModel.registrarAnotacion(elegido.id, tipoElegido, descripcion) {
                            descripcion = ""
                            tipo = null
                        }
                    }
                }) { Text("Registrar y firmar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmando = false }) { Text("Cancelar") }
            }
        )
    }
}
