package com.librompaz.app.ui.screen.inspectoria

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
 * Inspectoría: estudiantes con inasistencias reiteradas y anotaciones que requieren
 * intervención, más la hoja de vida completa del colegio. RF: "revisar la hoja de vida e
 * identificar comportamientos de riesgo, inasistencias reiteradas o anotaciones que requieran
 * intervención".
 *
 * TODO(equipo): inasistenciasReiteradas() y anotacionesQueRequierenIntervencion() hoy devuelven
 * listas vacías.
 */
@Composable
fun AlertasInspectoriaScreen(navController: NavHostController, viewModel: LibroMpazViewModel) {
    val todas by viewModel.anotaciones.collectAsState()
    val inasistencias = viewModel.inasistenciasReiteradas()
    val intervencion = viewModel.anotacionesQueRequierenIntervencion()

    Scaffold(topBar = { BarraSuperior("Alertas", navController) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Inasistencias reiteradas", style = MaterialTheme.typography.titleSmall)
            if (inasistencias.isEmpty()) Text("Sin alertas (o pendiente de implementar).", style = MaterialTheme.typography.bodySmall)
            inasistencias.forEach { (estudiante, ausencias) ->
                Text("• ${estudiante.nombreFicticio}: $ausencias ausencias", color = MaterialTheme.colorScheme.tertiary)
            }

            Text("Anotaciones que requieren intervención", style = MaterialTheme.typography.titleSmall)
            if (intervencion.isEmpty()) Text("Sin alertas (o pendiente de implementar).", style = MaterialTheme.typography.bodySmall)
            intervencion.forEach { anotacion ->
                val estudiante = viewModel.estudiantePorId(anotacion.estudianteId)
                Text("• ${estudiante?.nombreFicticio.orEmpty()}: ${anotacion.descripcion}", color = MaterialTheme.colorScheme.tertiary)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Hoja de vida del colegio (${todas.size})", style = MaterialTheme.typography.titleSmall)
            todas.forEach { anotacion ->
                val estudiante = viewModel.estudiantePorId(anotacion.estudianteId)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${estudiante?.nombreFicticio.orEmpty()} · ${anotacion.tipo.etiqueta}", style = MaterialTheme.typography.titleSmall)
                        Text(anotacion.descripcion, style = MaterialTheme.typography.bodySmall)
                        Text("${anotacion.fechaHora} · ${anotacion.firmadoPor}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
