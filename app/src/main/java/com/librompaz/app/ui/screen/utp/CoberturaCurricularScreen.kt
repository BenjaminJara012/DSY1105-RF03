package com.librompaz.app.ui.screen.utp

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.librompaz.app.domain.textoCobertura
import com.librompaz.app.model.Asignatura
import com.librompaz.app.model.Curso
import com.librompaz.app.ui.components.AvisoResultado
import com.librompaz.app.ui.components.BarraSuperior
import com.librompaz.app.ui.components.SelectorDesplegable
import com.librompaz.app.util.LectorDeVoz
import com.librompaz.app.viewmodel.Aviso
import com.librompaz.app.viewmodel.LibroMpazViewModel

/**
 * Módulo "Cobertura curricular" (UTP, RF-07, solo lectura): por curso y asignatura, qué OA ya se
 * trabajaron (según las clases que registran los docentes), cuándo fue la última vez, qué
 * contenidos se vieron y cuáles OA faltan. El botón "Escuchar resumen" lee la cobertura en voz
 * alta con el texto a voz del teléfono (recurso nativo).
 */
@Composable
fun CoberturaCurricularScreen(navController: NavHostController, viewModel: LibroMpazViewModel) {
    val cursos by viewModel.cursos.collectAsState()
    val asignaturas by viewModel.asignaturas.collectAsState()
    val sesiones by viewModel.sesiones.collectAsState()
    val todosLosOa by viewModel.objetivos.collectAsState()
    val contexto = LocalContext.current

    var curso by remember { mutableStateOf<Curso?>(null) }
    var asignatura by remember { mutableStateOf<Asignatura?>(null) }
    val cursoElegido = curso ?: cursos.firstOrNull()
    val asignaturaElegida = asignatura ?: asignaturas.firstOrNull()

    val objetivos = remember(todosLosOa, asignaturaElegida) { viewModel.objetivosDe(asignaturaElegida?.id) }
    val trabajados = remember(sesiones, cursoElegido, asignaturaElegida) {
        viewModel.oasTrabajados(cursoElegido?.id, asignaturaElegida?.id)
    }
    val ultimaFecha = remember(sesiones, cursoElegido, asignaturaElegida) {
        viewModel.ultimaFechaOa(cursoElegido?.id, asignaturaElegida?.id)
    }
    val clases = remember(sesiones, cursoElegido, asignaturaElegida) {
        viewModel.sesionesDe(cursoElegido?.id, asignaturaElegida?.id)
    }
    val cantidadTrabajados = objetivos.count { it.id in trabajados }
    val avance = if (objetivos.isEmpty()) 0f else cantidadTrabajados.toFloat() / objetivos.size
    // La barra de avance se mueve con una animación al cambiar de curso o asignatura.
    val avanceAnimado by animateFloatAsState(targetValue = avance, animationSpec = tween(600), label = "avance")

    // Recurso nativo: texto a voz. Se libera al salir de la pantalla.
    val lector = remember { LectorDeVoz(contexto) }
    DisposableEffect(Unit) { onDispose { lector.cerrar() } }
    var avisoVoz by remember { mutableStateOf<Aviso?>(null) }
    var contadorAvisos by remember { mutableStateOf(0L) }

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
                opciones = cursos,
                seleccion = cursoElegido,
                textoDe = { it.nombre },
                onSeleccion = { curso = it },
                icono = Icons.Filled.Info
            )
            SelectorDesplegable(
                etiqueta = "Asignatura",
                opciones = asignaturas,
                seleccion = asignaturaElegida,
                textoDe = { it.nombre },
                onSeleccion = { asignatura = it },
                icono = Icons.Filled.DateRange
            )

            Text(textoCobertura(cantidadTrabajados, objetivos.size), style = MaterialTheme.typography.titleMedium)
            LinearProgressIndicator(
                progress = { avanceAnimado },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )

            OutlinedButton(
                enabled = objetivos.isNotEmpty(),
                onClick = {
                    val pendientes = objetivos.filter { it.id !in trabajados }
                    val texto = "Cobertura de ${cursoElegido?.nombre.orEmpty()}, ${asignaturaElegida?.nombre.orEmpty()}. " +
                        "Se trabajaron $cantidadTrabajados de ${objetivos.size} objetivos de aprendizaje. " +
                        if (pendientes.isEmpty()) "No hay objetivos pendientes."
                        else "Pendientes: ${pendientes.joinToString(", ") { it.codigo }}."
                    if (!lector.leer(texto)) {
                        contadorAvisos += 1
                        avisoVoz = Aviso("La voz del teléfono no está disponible en español.", true, contadorAvisos)
                    }
                }
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Text("Escuchar resumen", modifier = Modifier.padding(start = 8.dp))
            }
            AvisoResultado(aviso = avisoVoz, onOcultar = { avisoVoz = null })

            objetivos.forEach { oa ->
                val hecho = oa.id in trabajados
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (hecho) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = if (hecho) "Trabajado" else "Pendiente",
                            tint = if (hecho) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column {
                            Text(oa.codigo, style = MaterialTheme.typography.titleSmall)
                            Text(oa.descripcion, style = MaterialTheme.typography.bodySmall)
                            Text(
                                if (hecho) "Trabajado" + (ultimaFecha[oa.id]?.let { " · última clase: $it" } ?: "") else "Pendiente",
                                color = if (hecho) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
            if (objetivos.isEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    Text("Sin OA cargados para esta asignatura.", style = MaterialTheme.typography.bodyMedium)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Contenidos trabajados (${clases.size})", style = MaterialTheme.typography.titleSmall)
            if (clases.isEmpty()) {
                Text("Todavía no hay clases registradas para este curso y asignatura.", style = MaterialTheme.typography.bodySmall)
            }
            clases.forEach { clase ->
                val codigos = clase.oaIds.mapNotNull { id -> objetivos.find { it.id == id }?.codigo }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${clase.fecha} · ${clase.bloque}", style = MaterialTheme.typography.titleSmall)
                        Text(clase.contenido, style = MaterialTheme.typography.bodySmall)
                        Text(
                            "OA: ${codigos.joinToString(", ")} · Registró: ${clase.registradoPor}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
