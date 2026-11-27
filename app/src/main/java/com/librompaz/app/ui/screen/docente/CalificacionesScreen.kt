package com.librompaz.app.ui.screen.docente

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.librompaz.app.domain.ErroresEvaluacion
import com.librompaz.app.ui.components.AvisoDelSistema
import com.librompaz.app.ui.components.BarraSuperior
import com.librompaz.app.ui.components.CampoFecha
import com.librompaz.app.ui.components.CampoTexto
import com.librompaz.app.ui.components.SelectorDesplegable
import com.librompaz.app.util.hoy
import com.librompaz.app.viewmodel.LibroMpazViewModel

/**
 * Módulo "Calificaciones" (docente, RF-08): crear un instrumento de evaluación y registrar las
 * notas de cada estudiante (escala chilena 1,0 a 7,0, con coma o punto). Cada nota se valida
 * mientras se escribe (el error aparece bajo el campo). Pide confirmación antes de reemplazar
 * notas o de eliminar una evaluación (RNF-03).
 */
@Composable
fun CalificacionesScreen(navController: NavHostController, viewModel: LibroMpazViewModel) {
    val cursoId by viewModel.cursoSeleccionadoId.collectAsState()
    val asignaturaId by viewModel.asignaturaSeleccionadaId.collectAsState()
    val todas by viewModel.evaluaciones.collectAsState()
    val todasLasNotas by viewModel.calificaciones.collectAsState()
    val todos by viewModel.estudiantes.collectAsState()
    val guardando by viewModel.guardando.collectAsState()
    val evaluaciones = remember(todas, cursoId, asignaturaId) { viewModel.evaluacionesDe(cursoId, asignaturaId) }
    val estudiantes = remember(todos, cursoId) { viewModel.estudiantesDelCurso(cursoId) }

    var evaluacionId by rememberSaveable { mutableStateOf<String?>(null) }
    val evaluacion = remember(evaluaciones, evaluacionId) { evaluaciones.find { it.id == evaluacionId } }
    var nuevoNombre by rememberSaveable { mutableStateOf("") }
    var nuevaFecha by rememberSaveable { mutableStateOf(hoy()) }
    var erroresEvaluacion by remember { mutableStateOf(ErroresEvaluacion()) }

    // Notas escritas (idEstudiante -> texto). Parten con lo ya guardado de la evaluación elegida.
    val guardadas = remember(todasLasNotas, evaluacionId) { viewModel.calificacionesDe(evaluacionId) }
    var notas by remember(evaluacionId) {
        mutableStateOf(guardadas.associate { it.estudianteId to it.nota.toString().replace('.', ',') })
    }
    var confirmandoNotas by remember { mutableStateOf(false) }
    var confirmandoEliminar by remember { mutableStateOf(false) }

    val hayNotaConError = notas.values.any { viewModel.validarNota(it) != null }
    val hayNotasEscritas = notas.values.any { it.isNotBlank() }

    Scaffold(topBar = { BarraSuperior("Calificaciones", navController) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Nueva evaluación", style = MaterialTheme.typography.titleSmall)
            CampoTexto(
                valor = nuevoNombre,
                onValorChange = {
                    nuevoNombre = it
                    erroresEvaluacion = erroresEvaluacion.copy(nombre = null)
                },
                etiqueta = "Nombre del instrumento",
                marcador = "Ej: Prueba unidad 2",
                icono = Icons.Filled.Edit,
                error = erroresEvaluacion.nombre,
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoFecha(
                    etiqueta = "Fecha",
                    fecha = nuevaFecha,
                    onFechaChange = {
                        nuevaFecha = it
                        erroresEvaluacion = erroresEvaluacion.copy(fecha = null, nombre = null)
                    },
                    error = erroresEvaluacion.fecha,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    enabled = !guardando,
                    modifier = Modifier.padding(top = 8.dp),
                    onClick = {
                        erroresEvaluacion = viewModel.crearEvaluacion(
                            cursoId.orEmpty(), asignaturaId.orEmpty(), nuevoNombre, nuevaFecha
                        ) { nuevoNombre = "" }
                    }
                ) { Text("Crear") }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                SelectorDesplegable(
                    etiqueta = "Evaluación",
                    opciones = evaluaciones,
                    seleccion = evaluacion,
                    textoDe = { "${it.nombreInstrumento} (${it.fecha})" },
                    onSeleccion = { evaluacionId = it.id },
                    textoVacio = if (evaluaciones.isEmpty()) "Aún no hay evaluaciones" else "Elige una evaluación",
                    icono = Icons.Filled.Grade,
                    modifier = Modifier.weight(1f)
                )
                if (evaluacion != null) {
                    IconButton(onClick = { confirmandoEliminar = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar evaluación", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // La planilla de notas aparece con una animación al elegir una evaluación.
            AnimatedVisibility(
                visible = evaluacion != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Notas ingresadas: ${notas.values.count { it.isNotBlank() }} de ${estudiantes.size}",
                        style = MaterialTheme.typography.labelLarge
                    )
                    estudiantes.forEach { estudiante ->
                        val texto = notas[estudiante.id].orEmpty()
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                estudiante.nombreFicticio,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(top = 18.dp)
                            )
                            CampoTexto(
                                valor = texto,
                                onValorChange = { notas = notas + (estudiante.id to it) },
                                etiqueta = "Nota",
                                error = viewModel.validarNota(texto),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.width(160.dp)
                            )
                        }
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        // Se puede guardar si ninguna nota tiene error y hay algo que guardar o corregir.
                        enabled = !guardando && !hayNotaConError && (hayNotasEscritas || guardadas.isNotEmpty()),
                        onClick = { confirmandoNotas = true }
                    ) {
                        if (guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Guardar notas")
                        }
                    }
                }
            }

            AvisoDelSistema(viewModel)
        }
    }

    if (confirmandoNotas) {
        AlertDialog(
            onDismissRequest = { confirmandoNotas = false },
            title = { Text(if (guardadas.isEmpty()) "¿Guardar notas?" else "¿Reemplazar notas?") },
            text = {
                Text(
                    if (guardadas.isEmpty()) "Se guardarán las notas de \"${evaluacion?.nombreInstrumento}\"."
                    else "Las notas que ya existían para esta evaluación se van a reemplazar y las que dejaste en blanco se eliminarán. El cambio quedará en la auditoría."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmandoNotas = false
                    evaluacionId?.let { id -> viewModel.guardarCalificaciones(id, notas) {} }
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmandoNotas = false }) { Text("Cancelar") }
            }
        )
    }

    if (confirmandoEliminar) {
        AlertDialog(
            onDismissRequest = { confirmandoEliminar = false },
            title = { Text("¿Eliminar evaluación?") },
            text = { Text("Se eliminará \"${evaluacion?.nombreInstrumento}\" y TODAS sus notas. Esta acción no se puede deshacer y quedará en la auditoría.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmandoEliminar = false
                    evaluacionId?.let { id -> viewModel.eliminarEvaluacion(id) { evaluacionId = null } }
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmandoEliminar = false }) { Text("Cancelar") }
            }
        )
    }
}
