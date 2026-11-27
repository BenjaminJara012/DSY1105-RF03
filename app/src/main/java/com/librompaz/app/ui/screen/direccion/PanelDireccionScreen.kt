package com.librompaz.app.ui.screen.direccion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import com.librompaz.app.domain.DIAS_VENTANA_CLASES
import com.librompaz.app.navigation.Rutas
import com.librompaz.app.ui.components.BarraSuperior
import com.librompaz.app.viewmodel.LibroMpazViewModel

/**
 * Módulo "Panel administrativo" (Dirección, RF-12, solo lectura): estado global del colegio con
 * métricas consolidadas por curso, indicadores de consistencia del registro y acceso a la
 * auditoría. Los datos son de solo lectura y no salen del dispositivo (RN-01).
 */
@Composable
fun PanelDireccionScreen(
    navController: NavHostController,
    viewModel: LibroMpazViewModel,
    onCerrarSesion: () -> Unit
) {
    val usuario by viewModel.usuarioActivo.collectAsState()
    val resumen by viewModel.resumenCursos.collectAsState()

    Scaffold(topBar = { BarraSuperior("Panel Dirección", navController, onCerrarSesion) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Hola, ${usuario?.nombre.orEmpty()}", style = MaterialTheme.typography.titleLarge)
            Text("Estado global por curso", style = MaterialTheme.typography.titleSmall)

            resumen.forEach { fila ->
                val curso = viewModel.cursoPorId(fila.cursoId)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(curso?.nombre.orEmpty(), style = MaterialTheme.typography.titleMedium)
                        Metrica("Asistencia", fila.porcentajeAsistencia?.let { "$it %" })
                        Metrica("Clases registradas", fila.sesionesRegistradas.toString())
                        Metrica("Notas registradas", fila.calificacionesRegistradas.toString())
                        Metrica("Anotaciones negativas", fila.anotacionesNegativas.toString())
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        Metrica("Días con asistencia incompleta", fila.diasAsistenciaIncompleta.toString())
                        Metrica(
                            "Cargas sin clases en $DIAS_VENTANA_CLASES días",
                            fila.cargasSinClasesRecientes.toString()
                        )
                    }
                }
            }

            Button(modifier = Modifier.fillMaxWidth(), onClick = { navController.navigate(Rutas.DIR_AUDITORIA) }) {
                Text("Registro de auditoría")
            }
        }
    }
}

@Composable
private fun Metrica(nombre: String, valor: String?) {
    Row {
        Text("$nombre: ", style = MaterialTheme.typography.bodyMedium)
        Text(valor ?: "—", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
    }
}
