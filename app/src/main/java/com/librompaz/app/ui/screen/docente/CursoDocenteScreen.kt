package com.librompaz.app.ui.screen.docente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
    val todos by viewModel.estudiantes.collectAsState()
    val curso = viewModel.cursoPorId(cursoId)
    val asignatura = viewModel.asignaturaPorId(asignaturaId)
    val cantidadEstudiantes = remember(todos, cursoId) { viewModel.estudiantesDelCurso(cursoId).size }

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

            BotonGrande("Asistencia del día", Icons.Filled.CheckCircle) { navController.navigate(Rutas.ASISTENCIA) }
            BotonGrande("Registro de clase (OA y contenidos)", Icons.Filled.DateRange) { navController.navigate(Rutas.DOC_CLASE) }
            BotonGrande("Calificaciones", Icons.Filled.Grade) { navController.navigate(Rutas.DOC_CALIFICACIONES) }
            BotonGrande("Hoja de vida", Icons.Filled.Person) { navController.navigate(Rutas.DOC_HOJA_VIDA) }
        }
    }
}

@Composable
private fun BotonGrande(texto: String, icono: ImageVector, onClick: () -> Unit) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        onClick = onClick
    ) {
        Icon(icono, contentDescription = null, modifier = Modifier.padding(end = 8.dp).size(ButtonDefaults.IconSize))
        Text(texto)
    }
}
