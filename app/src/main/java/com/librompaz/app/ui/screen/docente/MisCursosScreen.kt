package com.librompaz.app.ui.screen.docente

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.librompaz.app.navigation.Rutas
import com.librompaz.app.ui.components.BarraSuperior
import com.librompaz.app.viewmodel.LibroMpazViewModel

/**
 * Pantalla principal del Docente: SOLO sus cursos y asignaturas asignados (carga docente).
 * RF-01 (RBAC): viewModel.misCargas consulta Room filtrando por el id del docente autenticado.
 */
@Composable
fun MisCursosScreen(
    navController: NavHostController,
    viewModel: LibroMpazViewModel,
    onCerrarSesion: () -> Unit
) {
    val usuario by viewModel.usuarioActivo.collectAsState()
    val cargas by viewModel.misCargas.collectAsState()
    // Se observan para redibujar cuando Room termine de cargar los nombres.
    val cursos by viewModel.cursos.collectAsState()
    val asignaturas by viewModel.asignaturas.collectAsState()

    Scaffold(topBar = { BarraSuperior("Mis cursos", navController, onCerrarSesion) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Hola, ${usuario?.nombre.orEmpty()}", style = MaterialTheme.typography.titleLarge)
            Text(
                "Elige un curso y asignatura para registrar.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            if (cargas.isEmpty()) {
                Text("No tienes cursos asignados.", style = MaterialTheme.typography.bodyMedium)
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(cargas, key = { it.id }) { carga ->
                    val curso = cursos.find { it.id == carga.cursoId }
                    val asignatura = asignaturas.find { it.id == carga.asignaturaId }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.seleccionarCurso(carga.cursoId, carga.asignaturaId)
                                navController.navigate(Rutas.DOC_CURSO)
                            }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(curso?.nombre.orEmpty(), style = MaterialTheme.typography.titleMedium)
                                Text(asignatura?.nombre.orEmpty(), style = MaterialTheme.typography.bodyMedium)
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}
