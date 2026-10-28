package com.librompaz.app.ui.screen.docente

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.librompaz.app.navigation.Rutas
import com.librompaz.app.ui.components.BarraSuperior
import com.librompaz.app.viewmodel.LibroMpazViewModel

/**
 * Pantalla principal del Docente: sus cursos y asignaturas asignados (carga docente).
 *
 * TODO(equipo): viewModel.misCargas() hoy muestra TODAS las cargas, incluso las de otros
 * docentes. RF: el docente solo ve lo que tiene asignado formalmente (RBAC).
 */
@Composable
fun MisCursosScreen(
    navController: NavHostController,
    viewModel: LibroMpazViewModel,
    onCerrarSesion: () -> Unit
) {
    val usuario by viewModel.usuarioActivo.collectAsState()
    val cargas = viewModel.misCargas(usuario)

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
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(cargas) { carga ->
                    val curso = viewModel.cursoPorId(carga.cursoId)
                    val asignatura = viewModel.asignaturaPorId(carga.asignaturaId)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.seleccionarCurso(carga.cursoId, carga.asignaturaId)
                                navController.navigate(Rutas.DOC_CURSO)
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(curso?.nombre.orEmpty(), style = MaterialTheme.typography.titleMedium)
                            Text(asignatura?.nombre.orEmpty(), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
