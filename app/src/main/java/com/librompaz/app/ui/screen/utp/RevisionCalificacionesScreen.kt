package com.librompaz.app.ui.screen.utp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.librompaz.app.ui.components.BarraSuperior
import com.librompaz.app.viewmodel.LibroMpazViewModel

/**
 * Módulo "Calificaciones" (UTP): revisar el ingreso de notas e identificar desfases u omisiones
 * de registro. RF: "UTP debe poder revisar el ingreso de calificaciones".
 *
 * TODO(equipo): desfasesCalificaciones() hoy devuelve una lista vacía.
 */
@Composable
fun RevisionCalificacionesScreen(navController: NavHostController, viewModel: LibroMpazViewModel) {
    val evaluaciones by viewModel.evaluaciones.collectAsState()
    val desfases = viewModel.desfasesCalificaciones()

    Scaffold(topBar = { BarraSuperior("Revisión de calificaciones", navController) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Desfases u omisiones detectados", style = MaterialTheme.typography.titleSmall)
            if (desfases.isEmpty()) {
                Text(
                    "Sin alertas (o la detección todavía no está implementada: ver desfasesCalificaciones()).",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            desfases.forEach { alerta ->
                Text("• $alerta", color = MaterialTheme.colorScheme.tertiary)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Evaluaciones registradas (${evaluaciones.size})", style = MaterialTheme.typography.titleSmall)
            evaluaciones.forEach { evaluacion ->
                val curso = viewModel.cursoPorId(evaluacion.cursoId)
                val asignatura = viewModel.asignaturaPorId(evaluacion.asignaturaId)
                val notas = viewModel.calificacionesDe(evaluacion.id).size
                val total = viewModel.estudiantesDelCurso(evaluacion.cursoId).size
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(evaluacion.nombreInstrumento, style = MaterialTheme.typography.titleSmall)
                        Text("${curso?.nombre.orEmpty()} · ${asignatura?.nombre.orEmpty()} · ${evaluacion.fecha}", style = MaterialTheme.typography.bodySmall)
                        Text("Notas ingresadas: $notas de $total", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
