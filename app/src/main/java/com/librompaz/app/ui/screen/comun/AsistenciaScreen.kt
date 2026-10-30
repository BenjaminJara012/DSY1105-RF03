package com.librompaz.app.ui.screen.comun

import androidx.compose.runtime.LaunchedEffect
import com.librompaz.app.ui.components.MensajeAsistencia
import com.librompaz.app.ui.components.TipoMensajeAsistencia
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.librompaz.app.model.EstadoAsistencia
import com.librompaz.app.ui.components.BarraSuperior
import com.librompaz.app.util.hoy
import com.librompaz.app.viewmodel.LibroMpazViewModel
import android.app.DatePickerDialog
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.flow.first


/**
 * Módulo "Asistencia": registro diario del curso con cuatro estados (P presente, A ausente,
 * T atrasado, J justificado). Pensada para resolverse en menos de un minuto: todos parten
 * "presente" y solo se toca a quienes no lo están. La usan el Docente (su curso) e Inspectoría
 * (cualquier curso).
 *
 * Pide confirmación antes de guardar porque puede sobrescribir un registro (RNF del caso).
 *
 * TODO(equipo): asistenciaGuardada() hoy no carga lo ya registrado y guardarAsistencia() no
 * guarda nada.
 */
@Composable
fun AsistenciaScreen(navController: NavHostController, viewModel: LibroMpazViewModel) {
    val cursoId by viewModel.cursoSeleccionadoId.collectAsState()
    val curso = viewModel.cursoPorId(cursoId)
    val estudiantes = viewModel.estudiantesDelCurso(cursoId)
    // Observa los registros recuperados desde Room.

    // Indica si Room está consultando o guardando información.
    val cargandoAsistencia by
    viewModel.cargandoAsistencia.collectAsState()

    // Mensajes reales enviados por el ViewModel.
    val errorAsistencia by
    viewModel.errorAsistencia.collectAsState()

    val mensajeAsistencia by
    viewModel.mensajeAsistencia.collectAsState()

    var fecha by remember { mutableStateOf(hoy()) }
    val context = LocalContext.current

    /**
     * Abre el calendario nativo de Android.
     *
     * Permite seleccionar una fecha de asistencia
     * sin tener que escribirla manualmente.
     * No permite seleccionar fechas futuras.
     */
    fun mostrarCalendario() {

        // Recuperamos la fecha que aparece actualmente.
        val partes = fecha.split("-")

        val calendario = Calendar.getInstance().apply {
            set(
                partes[0].toInt(),
                partes[1].toInt() - 1,
                partes[2].toInt()
            )
        }

        // Creamos el selector de fecha de Android.
        DatePickerDialog(
            context,
            { _, anio, mes, dia ->

                // Convertimos la fecha al formato yyyy-MM-dd.
                fecha = String.format(
                    Locale.ROOT,
                    "%04d-%02d-%02d",
                    anio,
                    mes + 1,
                    dia
                )
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        ).apply {

            // Impide seleccionar días posteriores a hoy.
            datePicker.maxDate = System.currentTimeMillis()

        }.show()
    }

    /**
     * Estados que el usuario puede modificar en pantalla.
     *
     * Comienzan vacíos y después reciben la información
     * recuperada desde Room.
     */
    /**
     * Estados que el usuario está visualizando y modificando.
     *
     * Se reinician al cambiar de curso o fecha para
     * no mostrar registros de otra jornada.
     */
    var estados by remember(cursoId, fecha) {
        mutableStateOf<Map<String, EstadoAsistencia>>(emptyMap())
    }

    /**
     * Mantiene bloqueada la pantalla hasta que termine
     * la consulta inicial de la fecha seleccionada.
     */
    var esperandoConsulta by remember(cursoId, fecha) {
        mutableStateOf(true)
    }

    /**
     * Consulta Room al abrir la pantalla o cambiar
     * el curso o la fecha.
     */
    LaunchedEffect(cursoId, fecha) {

        // Inicia la consulta en el ViewModel.
        viewModel.cargarAsistencia(
            cursoId.orEmpty(),
            fecha
        )

        // Espera a que el ViewModel termine la operación.
        viewModel.cargandoAsistencia.first { cargando ->
            !cargando
        }

        // Recupera los estados de la consulta terminada.
        estados = viewModel.estadosAsistenciaGuardada.value

        // Permite nuevamente interactuar con la pantalla.
        esperandoConsulta = false
    }

    /**
     * Bloquea controles durante consultas y guardados.
     */
    val controlesBloqueados =
        cargandoAsistencia || esperandoConsulta





    /**
     * Calcula la cantidad de estudiantes por estado.
     * Los estudiantes sin selección explícita
     * se consideran presentes por defecto.
     */
    val presentes = estudiantes.count {
        (estados[it.id] ?: EstadoAsistencia.PRESENTE) ==
                EstadoAsistencia.PRESENTE
    }

    val ausentes = estudiantes.count {
        estados[it.id] == EstadoAsistencia.AUSENTE
    }

    val atrasados = estudiantes.count {
        estados[it.id] == EstadoAsistencia.ATRASADO
    }

    val justificados = estudiantes.count {
        estados[it.id] == EstadoAsistencia.JUSTIFICADO
    }
    var confirmando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { BarraSuperior("Asistencia · ${curso?.nombre.orEmpty()}", navController) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(value = fecha, onValueChange = {},
                readOnly = true, label = { Text("Fecha de asistencia") },
                singleLine = true,

                trailingIcon = { TextButton(onClick = { mostrarCalendario() }, enabled = !controlesBloqueados
                    ) { Text("Elegir") }
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            Text(
                "P = presente · A = ausente · T = atrasado · J = justificado",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            /**
             * Resumen visual de la asistencia.
             * Se actualiza al cambiar los estados.
             */
            Text(
                text = "Presentes: $presentes  |  Ausentes: $ausentes",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Text(
                text = "Atrasados: $atrasados  |  Justificados: $justificados",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(estudiantes) { estudiante ->
                    val actual = estados[estudiante.id] ?: EstadoAsistencia.PRESENTE
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(estudiante.nombreFicticio, modifier = Modifier.weight(1f))
                        EstadoAsistencia.entries.forEach { opcion ->
                            BotonEstado(
                                texto = opcion.abreviatura,
                                seleccionado = actual == opcion,
                                habilitado = !controlesBloqueados,
                                onClick = { estados = estados + (estudiante.id to opcion) }
                            )
                        }
                    }
                }
            }

            // Indica que Room está realizando una operación.
            if (controlesBloqueados) {
                Text(
                    text = "Procesando asistencia...",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Muestra errores reales de validación o persistencia.
            errorAsistencia?.let { error ->
                MensajeAsistencia(
                    texto = error,
                    tipo = TipoMensajeAsistencia.ERROR
                )
            }

            // Solo muestra éxito cuando el ViewModel
            // confirma que Room terminó el guardado.
            mensajeAsistencia?.let { mensaje ->
                MensajeAsistencia(
                    texto = mensaje,
                    tipo = TipoMensajeAsistencia.EXITO
                )
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                enabled = estudiantes.isNotEmpty() && !controlesBloqueados,
                onClick = { confirmando = true }
            ) { Text("Guardar asistencia") }
        }
    }

    if (confirmando) {
        AlertDialog(
            onDismissRequest = { confirmando = false },
            title = { Text("¿Guardar asistencia?") },
            text = { Text("Si ya existía un registro para esta fecha, se va a sobrescribir.") },
            confirmButton = {
                TextButton(onClick = {
                    // Los estudiantes que no se tocaron quedan como PRESENTE.
                    val completo = estudiantes.associate { it.id to (estados[it.id] ?: EstadoAsistencia.PRESENTE) }
                    // Envía los estados al ViewModel para guardarlos en Room.
                    viewModel.guardarAsistencia(cursoId.orEmpty(), fecha, completo)
                    // Cierra el cuadro de confirmación.
                    // El resultado real llegará mediante los StateFlow.
                    confirmando = false
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmando = false }) { Text("Cancelar") }
            }
        )
    }
}

/**
 * Botón para seleccionar un estado de asistencia.
 *
 * Al seleccionar un estado:
 * - Cambia suavemente el color del botón.
 * - Anima ligeramente su tamaño.
 * - Mantiene visible la opción seleccionada.
 *
 * @param texto Letra del estado: P, A, T o J.
 * @param seleccionado Indica si el estado está activo.
 * @param onClick Acción ejecutada al presionar el botón.
 */
@Composable
private fun BotonEstado(
    texto: String,
    seleccionado: Boolean,
    habilitado: Boolean,
    onClick: () -> Unit
) {
    val colores = MaterialTheme.colorScheme

    // Animación del color de fondo.
    val fondo by animateColorAsState(
        targetValue = if (seleccionado) {
            colores.primary
        } else {
            colores.surface
        },
        label = "Color fondo asistencia"
    )

    // Animación del color del texto.
    val textoColor by animateColorAsState(
        targetValue = if (seleccionado) {
            colores.onPrimary
        } else {
            colores.onSurface
        },
        label = "Color texto asistencia"
    )

    // El botón seleccionado aumenta ligeramente su escala.
    val escala by animateFloatAsState(
        targetValue = if (seleccionado) 1f else 0.94f,
        label = "Escala boton asistencia"
    )

    OutlinedButton(
        onClick = onClick,
        enabled = habilitado,
        modifier = Modifier
            .padding(start = 4.dp)
            .size(44.dp)
            .scale(escala),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = fondo,
            contentColor = textoColor
        ),
        border = BorderStroke(
            1.dp,
            if (seleccionado) colores.primary
            else colores.outline
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(texto)
    }
}
