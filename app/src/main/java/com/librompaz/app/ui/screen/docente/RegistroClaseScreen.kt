package com.librompaz.app.ui.screen.docente

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.librompaz.app.domain.ErroresSesion
import com.librompaz.app.ui.components.AvisoDelSistema
import com.librompaz.app.ui.components.BarraSuperior
import com.librompaz.app.ui.components.CampoFecha
import com.librompaz.app.ui.components.CampoTexto
import com.librompaz.app.util.hoy
import com.librompaz.app.viewmodel.LibroMpazViewModel

/**
 * Módulo "Cobertura curricular" (docente, RF-06): registrar los OA y contenidos trabajados en una
 * sesión o bloque, y ver las clases ya registradas. El ViewModel valida que haya fecha, bloque, al
 * menos un OA y contenido, y que no se repita la combinación curso + asignatura + fecha + bloque;
 * los errores se muestran junto a cada campo.
 */
@Composable
fun RegistroClaseScreen(navController: NavHostController, viewModel: LibroMpazViewModel) {
    val cursoId by viewModel.cursoSeleccionadoId.collectAsState()
    val asignaturaId by viewModel.asignaturaSeleccionadaId.collectAsState()
    val todas by viewModel.sesiones.collectAsState()
    val todosLosOa by viewModel.objetivos.collectAsState()
    val guardando by viewModel.guardando.collectAsState()
    val objetivos = remember(todosLosOa, asignaturaId) { viewModel.objetivosDe(asignaturaId) }
    val registradas = remember(todas, cursoId, asignaturaId) { viewModel.sesionesDe(cursoId, asignaturaId) }
    val curso = viewModel.cursoPorId(cursoId)
    val asignatura = viewModel.asignaturaPorId(asignaturaId)

    var fecha by rememberSaveable { mutableStateOf(hoy()) }
    var bloque by rememberSaveable { mutableStateOf("") }
    var seleccionados by remember { mutableStateOf(setOf<String>()) }
    var contenido by rememberSaveable { mutableStateOf("") }
    var errores by remember { mutableStateOf(ErroresSesion()) }

    Scaffold(topBar = { BarraSuperior("Registro de clase", navController) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "${curso?.nombre.orEmpty()} · ${asignatura?.nombre.orEmpty()}",
                style = MaterialTheme.typography.titleMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoFecha(
                    etiqueta = "Fecha",
                    fecha = fecha,
                    onFechaChange = {
                        fecha = it
                        errores = errores.copy(fecha = null, bloque = null)
                    },
                    error = errores.fecha,
                    modifier = Modifier.weight(1f)
                )
                CampoTexto(
                    valor = bloque,
                    onValorChange = {
                        bloque = it
                        errores = errores.copy(bloque = null)
                    },
                    etiqueta = "Bloque",
                    marcador = "Bloque 1",
                    icono = Icons.Filled.Schedule,
                    error = errores.bloque,
                    modifier = Modifier.weight(1f)
                )
            }

            Text("Objetivos de Aprendizaje trabajados", style = MaterialTheme.typography.titleSmall)
            objetivos.forEach { oa ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = oa.id in seleccionados,
                        onCheckedChange = { marcado ->
                            seleccionados = if (marcado) seleccionados + oa.id else seleccionados - oa.id
                            errores = errores.copy(objetivos = null)
                        }
                    )
                    Column {
                        Text(oa.codigo, style = MaterialTheme.typography.labelMedium)
                        Text(oa.descripcion, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            errores.objetivos?.let {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        Icons.Filled.ErrorOutline,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            CampoTexto(
                valor = contenido,
                onValorChange = {
                    contenido = it
                    errores = errores.copy(contenido = null)
                },
                etiqueta = "Contenidos abordados",
                icono = Icons.Filled.Edit,
                singleLine = false,
                minLines = 3,
                error = errores.contenido,
                modifier = Modifier.fillMaxWidth()
            )

            AvisoDelSistema(viewModel)

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !guardando,
                onClick = {
                    errores = viewModel.guardarSesion(
                        cursoId.orEmpty(), asignaturaId.orEmpty(), fecha, bloque, seleccionados.toList(), contenido
                    ) {
                        bloque = ""
                        seleccionados = emptySet()
                        contenido = ""
                    }
                }
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Guardar clase")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Clases registradas (${registradas.size})", style = MaterialTheme.typography.titleSmall)
            if (registradas.isEmpty()) {
                Text("Todavía no hay clases registradas.", style = MaterialTheme.typography.bodySmall)
            }
            registradas.forEach { sesion ->
                Card(modifier = Modifier.fillMaxWidth().animateContentSize()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${sesion.fecha} · ${sesion.bloque}", style = MaterialTheme.typography.titleSmall)
                        Text(sesion.contenido, style = MaterialTheme.typography.bodySmall)
                        val codigos = sesion.oaIds.mapNotNull { id -> objetivos.find { it.id == id }?.codigo }
                        Text(
                            "OA: ${codigos.joinToString(", ")} · Registró: ${sesion.registradoPor}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
