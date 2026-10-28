package com.librompaz.app.ui.screen.docente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.librompaz.app.model.Evaluacion
import com.librompaz.app.ui.components.BarraSuperior
import com.librompaz.app.ui.components.SelectorDesplegable
import com.librompaz.app.util.hoy
import com.librompaz.app.viewmodel.LibroMpazViewModel

/**
 * Módulo "Calificaciones" (docente): crear un instrumento de evaluación y registrar las notas
 * de cada estudiante (escala numérica chilena). Pide confirmación antes de guardar porque puede
 * sobrescribir notas (RNF del caso).
 *
 * TODO(equipo): crearEvaluacion(), validarNota() y guardarCalificaciones() todavía no hacen nada.
 */
@Composable
fun CalificacionesScreen(navController: NavHostController, viewModel: LibroMpazViewModel) {
    val cursoId by viewModel.cursoSeleccionadoId.collectAsState()
    val asignaturaId by viewModel.asignaturaSeleccionadaId.collectAsState()
    val todas by viewModel.evaluaciones.collectAsState()
    val evaluaciones = remember(todas, cursoId, asignaturaId) { viewModel.evaluacionesDe(cursoId, asignaturaId) }
    val estudiantes = viewModel.estudiantesDelCurso(cursoId)

    var evaluacion by remember { mutableStateOf<Evaluacion?>(null) }
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevaFecha by remember { mutableStateOf(hoy()) }
    var notas by remember(evaluacion) {
        mutableStateOf(viewModel.calificacionesDe(evaluacion?.id).associate { it.estudianteId to it.nota.toString() })
    }
    var confirmando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }

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
            OutlinedTextField(
                value = nuevoNombre,
                onValueChange = { nuevoNombre = it },
                label = { Text("Nombre del instrumento") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nuevaFecha,
                    onValueChange = { nuevaFecha = it },
                    label = { Text("Fecha") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(onClick = {
                    viewModel.crearEvaluacion(cursoId.orEmpty(), asignaturaId.orEmpty(), nuevoNombre.trim(), nuevaFecha)
                    mensaje = "Evaluación enviada a crear (cuando crearEvaluacion() esté implementada)."
                }) { Text("Crear") }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SelectorDesplegable(
                etiqueta = "Evaluación",
                opciones = evaluaciones,
                seleccion = evaluacion,
                textoDe = { "${it.nombreInstrumento} (${it.fecha})" },
                onSeleccion = { evaluacion = it },
                textoVacio = "Elige una evaluación"
            )

            if (evaluacion != null) {
                estudiantes.forEach { estudiante ->
                    val texto = notas[estudiante.id].orEmpty()
                    val error = if (texto.isBlank()) null else viewModel.validarNota(texto)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(estudiante.nombreFicticio, modifier = Modifier.weight(1f))
                        OutlinedTextField(
                            value = texto,
                            onValueChange = { notas = notas + (estudiante.id to it) },
                            label = { Text("Nota") },
                            singleLine = true,
                            isError = error != null,
                            supportingText = if (error != null) {
                                { Text(error) }
                            } else {
                                null
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.width(120.dp)
                        )
                    }
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { confirmando = true }
                ) { Text("Guardar notas") }
            }

            mensaje?.let { Text(it, color = MaterialTheme.colorScheme.secondary) }
        }
    }

    if (confirmando) {
        AlertDialog(
            onDismissRequest = { confirmando = false },
            title = { Text("¿Guardar notas?") },
            text = { Text("Las notas que ya existían para esta evaluación se van a sobrescribir.") },
            confirmButton = {
                TextButton(onClick = {
                    evaluacion?.let { viewModel.guardarCalificaciones(it.id, notas) }
                    confirmando = false
                    mensaje = "Notas enviadas a guardar (cuando guardarCalificaciones() esté implementada)."
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmando = false }) { Text("Cancelar") }
            }
        )
    }
}
