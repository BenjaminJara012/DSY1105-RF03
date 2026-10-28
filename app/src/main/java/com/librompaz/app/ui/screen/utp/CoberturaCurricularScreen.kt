package com.librompaz.app.ui.screen.utp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.librompaz.app.model.Asignatura
import com.librompaz.app.model.Curso
import com.librompaz.app.ui.components.BarraSuperior
import com.librompaz.app.ui.components.SelectorDesplegable
import com.librompaz.app.viewmodel.LibroMpazViewModel

/**
 * Módulo "Cobertura curricular" (UTP): por curso y asignatura, qué OA ya se trabajaron y cuáles
 * faltan. RF: "vistas analíticas consolidadas de cobertura curricular por curso".
 *
 * TODO(equipo): oasTrabajados() hoy devuelve un conjunto vacío (todo aparece "pendiente").
 */
@Composable
fun CoberturaCurricularScreen(navController: NavHostController, viewModel: LibroMpazViewModel) {
    var curso by remember { mutableStateOf<Curso?>(viewModel.cursos.firstOrNull()) }
    var asignatura by remember { mutableStateOf<Asignatura?>(viewModel.asignaturas.firstOrNull()) }

    val objetivos = viewModel.objetivosDe(asignatura?.id)
    val trabajados = viewModel.oasTrabajados(curso?.id, asignatura?.id)
    val avance = if (objetivos.isEmpty()) 0f else trabajados.size.toFloat() / objetivos.size

    Scaffold(topBar = { BarraSuperior("Cobertura curricular", navController) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SelectorDesplegable(
                etiqueta = "Curso",
                opciones = viewModel.cursos,
                seleccion = curso,
                textoDe = { it.nombre },
                onSeleccion = { curso = it }
            )
            SelectorDesplegable(
                etiqueta = "Asignatura",
                opciones = viewModel.asignaturas,
                seleccion = asignatura,
                textoDe = { it.nombre },
                onSeleccion = { asignatura = it }
            )

            Text("OA trabajados: ${trabajados.size} de ${objetivos.size}", style = MaterialTheme.typography.titleMedium)
            LinearProgressIndicator(progress = { avance }, modifier = Modifier.fillMaxWidth())

            objetivos.forEach { oa ->
                val hecho = oa.id in trabajados
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(oa.codigo, style = MaterialTheme.typography.titleSmall)
                        Text(oa.descripcion, style = MaterialTheme.typography.bodySmall)
                        Text(
                            if (hecho) "✔ Trabajado" else "Pendiente",
                            color = if (hecho) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}
