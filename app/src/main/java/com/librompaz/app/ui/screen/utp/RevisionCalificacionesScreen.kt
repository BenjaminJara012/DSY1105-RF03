package com.librompaz.app.ui.screen.utp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.librompaz.app.domain.TipoDesfase
import com.librompaz.app.ui.components.BarraSuperior
import com.librompaz.app.viewmodel.LibroMpazViewModel

/**
 * Módulo "Calificaciones" (UTP, RF-09, solo lectura): revisar el ingreso de notas e identificar
 * desfases u omisiones de registro (cargas sin evaluaciones, evaluaciones con notas faltantes y
 * evaluaciones atrasadas).
 */
@Composable
fun RevisionCalificacionesScreen(navController: NavHostController, viewModel: LibroMpazViewModel) {
    val evaluaciones by viewModel.evaluaciones.collectAsState()
    val calificaciones by viewModel.calificaciones.collectAsState()
    val desfases by viewModel.desfases.collectAsState()
    val estudiantes by viewModel.estudiantes.collectAsState()

    Scaffold(topBar = { BarraSuperior("Revisión de calificaciones", navController) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Desfases u omisiones detectados (${desfases.size})", style = MaterialTheme.typography.titleSmall)
            if (desfases.isEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        "Sin alertas: todas las cargas tienen evaluaciones con notas completas.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            desfases.forEach { alerta ->
                val grave = alerta.tipo == TipoDesfase.ATRASADA
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (grave) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (grave) Icons.Filled.Warning else Icons.Filled.Info,
                            contentDescription = if (grave) "Atrasada" else "Aviso",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(alerta.mensaje, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Evaluaciones registradas (${evaluaciones.size})", style = MaterialTheme.typography.titleSmall)
            evaluaciones.forEach { evaluacion ->
                val curso = viewModel.cursoPorId(evaluacion.cursoId)
                val asignatura = viewModel.asignaturaPorId(evaluacion.asignaturaId)
                val notas = remember(calificaciones, evaluacion) { viewModel.calificacionesDe(evaluacion.id) }
                val total = remember(estudiantes, evaluacion) { viewModel.estudiantesDelCurso(evaluacion.cursoId).size }
                val promedio = if (notas.isEmpty()) null else notas.map { it.nota }.average()
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(evaluacion.nombreInstrumento, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${curso?.nombre.orEmpty()} · ${asignatura?.nombre.orEmpty()} · ${evaluacion.fecha}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Notas ingresadas: ${notas.size} de $total" +
                                (promedio?.let { " · Promedio: ${String.format(java.util.Locale.ROOT, "%.1f", it).replace('.', ',')}" } ?: ""),
                            style = MaterialTheme.typography.labelMedium
                        )
                        LinearProgressIndicator(
                            progress = { if (total == 0) 0f else notas.size.toFloat() / total },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
