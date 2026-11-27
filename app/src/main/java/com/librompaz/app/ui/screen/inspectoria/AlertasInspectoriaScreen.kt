package com.librompaz.app.ui.screen.inspectoria

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.librompaz.app.domain.UMBRAL_ANOTACIONES_NEGATIVAS
import com.librompaz.app.domain.UMBRAL_INASISTENCIAS
import com.librompaz.app.model.TipoAnotacion
import com.librompaz.app.ui.components.BarraSuperior
import com.librompaz.app.viewmodel.LibroMpazViewModel

/**
 * Inspectoría (RF-11): estudiantes con inasistencias reiteradas y anotaciones que requieren
 * intervención, más la hoja de vida completa del colegio con filtro por tipo.
 */
@Composable
fun AlertasInspectoriaScreen(navController: NavHostController, viewModel: LibroMpazViewModel) {
    val todas by viewModel.anotaciones.collectAsState()
    val inasistencias by viewModel.alertasInasistencia.collectAsState()
    val intervencion by viewModel.alertasAnotaciones.collectAsState()

    var filtro by remember { mutableStateOf<TipoAnotacion?>(null) } // null = todas
    val hojaDeVida = remember(todas, filtro) { viewModel.anotacionesPorTipo(filtro) }

    Scaffold(topBar = { BarraSuperior("Alertas", navController) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Inasistencias reiteradas ($UMBRAL_INASISTENCIAS o más ausencias)",
                style = MaterialTheme.typography.titleSmall
            )
            if (inasistencias.isEmpty()) Text("Sin alertas.", style = MaterialTheme.typography.bodySmall)
            inasistencias.forEach { (estudiante, ausencias) ->
                TarjetaAlerta(
                    titulo = "${estudiante.nombreFicticio} · ${viewModel.cursoPorId(estudiante.cursoId)?.nombre.orEmpty()}",
                    detalle = "$ausencias ausencias"
                )
            }

            Text(
                "Anotaciones que requieren intervención ($UMBRAL_ANOTACIONES_NEGATIVAS o más negativas)",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (intervencion.isEmpty()) Text("Sin alertas.", style = MaterialTheme.typography.bodySmall)
            intervencion.forEach { anotacion ->
                TarjetaAlerta(
                    titulo = viewModel.estudiantePorId(anotacion.estudianteId)?.nombreFicticio.orEmpty(),
                    detalle = "${anotacion.fechaHora}: ${anotacion.descripcion}"
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Hoja de vida del colegio (${hojaDeVida.size})", style = MaterialTheme.typography.titleSmall)

            // Filtro por tipo de anotación.
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                FilterChip(selected = filtro == null, onClick = { filtro = null }, label = { Text("Todas") })
                TipoAnotacion.entries.forEach { tipo ->
                    FilterChip(selected = filtro == tipo, onClick = { filtro = tipo }, label = { Text(tipo.etiqueta) })
                }
            }

            hojaDeVida.forEach { anotacion ->
                val estudiante = viewModel.estudiantePorId(anotacion.estudianteId)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "${estudiante?.nombreFicticio.orEmpty()} · ${anotacion.tipo.etiqueta}",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(anotacion.descripcion, style = MaterialTheme.typography.bodySmall)
                        Text("${anotacion.fechaHora} · Firmó: ${anotacion.firmadoPor}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaAlerta(titulo: String, detalle: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleSmall)
            Text(detalle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
