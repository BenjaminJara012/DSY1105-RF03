package com.librompaz.app.ui.screen.docente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
 * Libro del curso elegido: acceso a los cuatro registros del docente (asistencia, clase con
 * OA, calificaciones y hoja de vida).
 */
@Composable
fun CursoDocenteScreen(navController: NavHostController, viewModel: LibroMpazViewModel) {
    val cursoId by viewModel.cursoSeleccionadoId.collectAsState()
    val asignaturaId by viewModel.asignaturaSeleccionadaId.collectAsState()
    val curso = viewModel.cursoPorId(cursoId)
    val asignatura = viewModel.asignaturaPorId(asignaturaId)
    val cantidadEstudiantes = viewModel.estudiantesDelCurso(cursoId).size

    Scaffold(topBar = { BarraSuperior(curso?.nombre ?: "Curso", navController) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(asignatura?.nombre.orEmpty(), style = MaterialTheme.typography.titleLarge)
            Text("$cantidadEstudiantes estudiantes", style = MaterialTheme.typography.bodyMedium)

            BotonGrande("Asistencia del día") { navController.navigate(Rutas.ASISTENCIA) }
            BotonGrande("Registro de clase (OA y contenidos)") { navController.navigate(Rutas.DOC_CLASE) }
            BotonGrande("Calificaciones") { navController.navigate(Rutas.DOC_CALIFICACIONES) }
            BotonGrande("Hoja de vida") { navController.navigate(Rutas.DOC_HOJA_VIDA) }
        }
    }
}

@Composable
private fun BotonGrande(texto: String, onClick: () -> Unit) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        onClick = onClick
    ) { Text(texto) }
}
