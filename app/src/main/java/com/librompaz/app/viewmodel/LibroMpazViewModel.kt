
package com.librompaz.app.viewmodel

import androidx.lifecycle.ViewModel
import com.librompaz.app.data.MockData
import com.librompaz.app.model.Anotacion
import com.librompaz.app.model.Asignatura
import com.librompaz.app.model.Calificacion
import com.librompaz.app.model.CargaDocente
import com.librompaz.app.model.Curso
import com.librompaz.app.model.EstadoAsistencia
import com.librompaz.app.model.Estudiante
import com.librompaz.app.model.Evaluacion
import com.librompaz.app.model.ObjetivoAprendizaje
import com.librompaz.app.model.Perfil
import com.librompaz.app.model.RegistroAuditoria
import com.librompaz.app.model.ResumenCurso
import com.librompaz.app.model.SesionClase
import com.librompaz.app.model.TipoAnotacion
import com.librompaz.app.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.librompaz.app.model.RegistroAsistencia
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import com.librompaz.app.data.local.AppDatabase
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.librompaz.app.data.local.AsistenciaEntity
import com.librompaz.app.data.local.AuditoriaEntity

/**
 * ViewModel principal de LibroMPAZ.
 *
 * Centraliza la sesión del usuario, los datos del colegio
 * y la lógica de los distintos módulos.
 *
 * Las funciones de creación y modificación deben incorporar
 * auditoría automática cuando se implemente esa funcionalidad.
 */
class LibroMpazViewModel(
    private val database: AppDatabase
) : ViewModel() {

    // =========================================================
    // AUTENTICACIÓN Y CONTROL DE ACCESO
    // =========================================================

    private val _usuarioActivo = MutableStateFlow<Usuario?>(null)
    val usuarioActivo: StateFlow<Usuario?> = _usuarioActivo.asStateFlow()

    private val _errorLogin = MutableStateFlow<String?>(null)
    val errorLogin: StateFlow<String?> = _errorLogin.asStateFlow()

    /**
     * Valida las credenciales del usuario contra los datos ficticios.
     *
     * Si los datos son correctos, guarda la sesión y devuelve true.
     * Si son incorrectos o están vacíos, devuelve false y muestra error.
     */
    fun login(usuario: String, clave: String): Boolean {

        _errorLogin.value = null

        if (usuario.isBlank() || clave.isBlank()) {
            _usuarioActivo.value = null
            _errorLogin.value = "Debes ingresar usuario y contraseña."
            return false
        }

        val usuarioEncontrado = MockData.usuarios.firstOrNull {
            it.usuario.equals(usuario.trim(), ignoreCase = true) &&
                    it.clave == clave
        }

        return if (usuarioEncontrado != null) {

            _usuarioActivo.value = usuarioEncontrado

            // Reinicia las selecciones anteriores al iniciar sesión.
            _cursoSeleccionadoId.value = null
            _asignaturaSeleccionadaId.value = null

            true

        } else {

            _usuarioActivo.value = null
            _errorLogin.value = "Usuario o contraseña incorrectos."

            false
        }
    }

    /**
     * Permite ingresar con un usuario ficticio de un perfil.
     * Función auxiliar para desarrollo y pruebas.
     */
    fun entrarModoDesarrollo(perfil: Perfil) {

        val usuario = MockData.usuarios.firstOrNull {
            it.perfil == perfil
        }

        if (usuario != null) {
            _usuarioActivo.value = usuario
            _errorLogin.value = null
            _cursoSeleccionadoId.value = null
            _asignaturaSeleccionadaId.value = null
        } else {
            _errorLogin.value = "No existe un usuario para ese perfil."
        }
    }

    /**
     * Cierra la sesión y limpia los datos de navegación.
     */
    fun logout() {

        _usuarioActivo.value = null
        _errorLogin.value = null
        _cursoSeleccionadoId.value = null
        _asignaturaSeleccionadaId.value = null
    }

    // =========================================================
    // DATOS DE REFERENCIA Y SELECCIÓN
    // =========================================================

    val cursos: List<Curso> = MockData.cursos
    val asignaturas: List<Asignatura> = MockData.asignaturas

    private val estudiantes: List<Estudiante> = MockData.estudiantes
    private val objetivos: List<ObjetivoAprendizaje> = MockData.objetivos

    private val _cursoSeleccionadoId =
        MutableStateFlow<String?>(null)

    val cursoSeleccionadoId: StateFlow<String?> =
        _cursoSeleccionadoId.asStateFlow()

    private val _asignaturaSeleccionadaId =
        MutableStateFlow<String?>(null)

    val asignaturaSeleccionadaId: StateFlow<String?> =
        _asignaturaSeleccionadaId.asStateFlow()

    /**
     * Guarda el curso y asignatura seleccionados.
     */
    fun seleccionarCurso(
        cursoId: String?,
        asignaturaId: String? = null
    ) {
        _cursoSeleccionadoId.value = cursoId
        _asignaturaSeleccionadaId.value = asignaturaId
    }

    fun cursoPorId(id: String?): Curso? =
        cursos.find { it.id == id }

    fun asignaturaPorId(id: String?): Asignatura? =
        asignaturas.find { it.id == id }

    fun estudiantePorId(id: String?): Estudiante? =
        estudiantes.find { it.id == id }

    fun estudiantesDelCurso(cursoId: String?): List<Estudiante> =
        estudiantes.filter { it.cursoId == cursoId }

    fun objetivosDe(asignaturaId: String?): List<ObjetivoAprendizaje> =
        objetivos.filter { it.asignaturaId == asignaturaId }

    /**
     * Devuelve exclusivamente las cargas asignadas al docente autenticado.
     *
     * Se utiliza docenteId para relacionar las cargas con el usuario.
     * Los perfiles que no son docentes no reciben cargas docentes.
     */
    fun misCargas(usuario: Usuario?): List<CargaDocente> {

        if (usuario == null || usuario.perfil != Perfil.DOCENTE) {
            return emptyList()
        }

        return MockData.cargas.filter { carga ->
            carga.docenteId == usuario.id
        }
    }

    /**
     * Comprueba si el docente puede ingresar al curso y asignatura.
     *
     * Si asignaturaId es null, comprueba solamente el curso.
     */
    fun puedeAccederAlCurso(
        usuario: Usuario?,
        cursoId: String?,
        asignaturaId: String? = null
    ): Boolean {

        if (usuario == null ||
            usuario.perfil != Perfil.DOCENTE ||
            cursoId == null
        ) {
            return false
        }

        return misCargas(usuario).any { carga ->

            carga.cursoId == cursoId &&
                    (
                            asignaturaId == null ||
                                    carga.asignaturaId == asignaturaId
                            )
        }
    }

    // =========================================================
    // MÓDULO ASISTENCIA
    // =========================================================

    // Lista observable con todos los registros de asistencia.
    private val _asistencias =
        MutableStateFlow<List<RegistroAsistencia>>(MockData.asistencias)

    val asistencias: StateFlow<List<RegistroAsistencia>> =
        _asistencias.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val registros = database.asistenciaDao().todas()

            if (registros.isNotEmpty()) {
                _asistencias.value = registros.mapNotNull { registro ->
                    val estado = runCatching {
                        EstadoAsistencia.valueOf(registro.estado)
                    }.getOrNull()

                    estado?.let {
                        RegistroAsistencia(
                            id = registro.id,
                            estudianteId = registro.estudianteId,
                            cursoId = registro.cursoId,
                            fecha = registro.fecha,
                            estado = it,
                            registradoPor = registro.registradoPor,
                            fechaHoraRegistro = registro.fechaHoraRegistro
                        )
                    }
                }
            }
        }
    }


    /**
     * Consulta la asistencia guardada de un curso en una fecha.
     * Devuelve un mapa con el ID del estudiante y su estado.
     */
    fun asistenciaGuardada(
        cursoId: String,
        fecha: String
    ): Map<String, EstadoAsistencia> {

        return _asistencias.value
            .filter {
                it.cursoId == cursoId && it.fecha == fecha
            }
            .associate {
                it.estudianteId to it.estado
            }
    }

    /**
     * Guarda la asistencia de un curso en una fecha.
     *
     * Cada registro incluye el usuario que realizó la operación
     * y la fecha y hora de registro.
     *
     * Si ya existía asistencia para ese curso y fecha,
     * se reemplazan los registros anteriores.
     */

    /**
     * Guarda o corrige la asistencia y genera un registro de auditoría.
     */
    fun guardarAsistencia(
        cursoId: String,
        fecha: String,
        estados: Map<String, EstadoAsistencia>
    ) {
        val usuario = _usuarioActivo.value ?: return

        if (cursoPorId(cursoId) == null) return

        val estudiantesValidos = estudiantesDelCurso(cursoId)
            .map { it.id }
            .toSet()

        val estadosValidos = estados.filterKeys {
            it in estudiantesValidos
        }

        if (estadosValidos.isEmpty()) return

        val fechaHoraActual = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        )

        val nuevosRegistros = estadosValidos.map { (estudianteId, estado) ->
            RegistroAsistencia(
                id = UUID.randomUUID().toString(),
                estudianteId = estudianteId,
                cursoId = cursoId,
                fecha = fecha,
                estado = estado,
                registradoPor = usuario.id,
                fechaHoraRegistro = fechaHoraActual
            )
        }

        val nuevoRegistroAuditoria = RegistroAuditoria(
            id = UUID.randomUUID().toString(),
            usuario = usuario.usuario,
            perfil = usuario.perfil,
            accion = "REGISTRO_ASISTENCIA",
            detalle = "Se registró o modificó la asistencia del curso $cursoId para la fecha $fecha.",
            fechaHora = fechaHoraActual
        )

        viewModelScope.launch(Dispatchers.IO) {
            val entidadesAsistencia = nuevosRegistros.map { registro ->
                AsistenciaEntity(
                    id = registro.id,
                    estudianteId = registro.estudianteId,
                    cursoId = registro.cursoId,
                    fecha = registro.fecha,
                    estado = registro.estado.name,
                    registradoPor = registro.registradoPor,
                    fechaHoraRegistro = registro.fechaHoraRegistro
                )
            }

            val entidadAuditoria = AuditoriaEntity(
                id = nuevoRegistroAuditoria.id,
                usuario = nuevoRegistroAuditoria.usuario,
                perfil = nuevoRegistroAuditoria.perfil.name,
                accion = nuevoRegistroAuditoria.accion,
                detalle = nuevoRegistroAuditoria.detalle,
                fechaHora = nuevoRegistroAuditoria.fechaHora
            )

            database.guardarConAuditoria(
                entidadesAsistencia,
                entidadAuditoria,
                cursoId,
                fecha
            )

            _asistencias.value = _asistencias.value
                .filterNot { it.cursoId == cursoId && it.fecha == fecha } +
                    nuevosRegistros

            _auditoria.value = _auditoria.value + nuevoRegistroAuditoria
        }
    }

    /**
     * Calcula el porcentaje de asistencia del curso.
     *
     * Se consideran presentes quienes tienen estado PRESENTE
     * o ATRASADO. Los registros JUSTIFICADO y AUSENTE no
     * se cuentan como asistencia efectiva.
     */
    fun porcentajeAsistencia(cursoId: String): Int? {

        val registros = _asistencias.value.filter {
            it.cursoId == cursoId
        }

        if (registros.isEmpty()) return null

        val presentes = registros.count {
            it.estado == EstadoAsistencia.PRESENTE ||
                    it.estado == EstadoAsistencia.ATRASADO
        }

        return (presentes * 100) / registros.size
    }

    /**
     * Busca estudiantes que acumulan tres o más ausencias.
     *
     * Devuelve el estudiante y la cantidad de ausencias.
     */
    fun inasistenciasReiteradas(): List<Pair<Estudiante, Int>> {

        val conteoPorEstudiante = _asistencias.value
            .filter {
                it.estado == EstadoAsistencia.AUSENTE
            }
            .groupingBy {
                it.estudianteId
            }
            .eachCount()

        return estudiantes.mapNotNull { estudiante ->

            val cantidad = conteoPorEstudiante[estudiante.id] ?: 0

            if (cantidad >= 3) {
                estudiante to cantidad
            } else {
                null
            }
        }
    }

    // =========================================================
    // MÓDULO COBERTURA CURRICULAR
    // =========================================================

    private val _sesiones =
        MutableStateFlow(MockData.sesiones)

    val sesiones: StateFlow<List<SesionClase>> =
        _sesiones.asStateFlow()

    fun sesionesDe(
        cursoId: String?,
        asignaturaId: String?
    ): List<SesionClase> {

        return _sesiones.value.filter {
            it.cursoId == cursoId &&
                    it.asignaturaId == asignaturaId
        }
    }

    fun guardarSesion(
        cursoId: String,
        asignaturaId: String,
        fecha: String,
        bloque: String,
        oaIds: List<String>,
        contenido: String
    ) {
        // Pendiente de implementación.
    }

    fun oasTrabajados(
        cursoId: String?,
        asignaturaId: String?
    ): Set<String> = emptySet()

    // =========================================================
    // MÓDULO CALIFICACIONES
    // =========================================================

    private val _evaluaciones =
        MutableStateFlow(MockData.evaluaciones)

    val evaluaciones: StateFlow<List<Evaluacion>> =
        _evaluaciones.asStateFlow()

    private val _calificaciones =
        MutableStateFlow(MockData.calificaciones)

    fun evaluacionesDe(
        cursoId: String?,
        asignaturaId: String?
    ): List<Evaluacion> {

        return _evaluaciones.value.filter {
            it.cursoId == cursoId &&
                    it.asignaturaId == asignaturaId
        }
    }

    fun calificacionesDe(
        evaluacionId: String?
    ): List<Calificacion> {

        return _calificaciones.value.filter {
            it.evaluacionId == evaluacionId
        }
    }

    fun crearEvaluacion(
        cursoId: String,
        asignaturaId: String,
        nombreInstrumento: String,
        fecha: String
    ) {
        // Pendiente de implementación.
    }

    fun validarNota(texto: String): String? = null

    fun guardarCalificaciones(
        evaluacionId: String,
        notas: Map<String, String>
    ) {
        // Pendiente de implementación.
    }

    fun desfasesCalificaciones(): List<String> =
        emptyList()

    // =========================================================
    // MÓDULO HOJA DE VIDA
    // =========================================================

    private val _anotaciones =
        MutableStateFlow(MockData.anotaciones)

    val anotaciones: StateFlow<List<Anotacion>> =
        _anotaciones.asStateFlow()

    fun anotacionesDe(
        estudianteId: String?
    ): List<Anotacion> {

        return _anotaciones.value.filter {
            it.estudianteId == estudianteId
        }
    }

    fun registrarAnotacion(
        estudianteId: String,
        tipo: TipoAnotacion,
        descripcion: String
    ) {
        // Pendiente de implementación.
    }

    fun anotacionesQueRequierenIntervencion(): List<Anotacion> =
        emptyList()

    // =========================================================
    // PANEL ADMINISTRATIVO Y AUDITORÍA
    // =========================================================

    private val _auditoria =
        MutableStateFlow(MockData.auditoria)

    val auditoria: StateFlow<List<RegistroAuditoria>> =
        _auditoria.asStateFlow()
    init {
        viewModelScope.launch(Dispatchers.IO) {
            database.auditoriaDao().todas().collect { registros ->
                _auditoria.value = registros.mapNotNull { registro ->
                    val perfil = runCatching {
                        Perfil.valueOf(registro.perfil)
                    }.getOrNull()

                    perfil?.let {
                        RegistroAuditoria(
                            id = registro.id,
                            usuario = registro.usuario,
                            perfil = it,
                            accion = registro.accion,
                            detalle = registro.detalle,
                            fechaHora = registro.fechaHora
                        )
                    }
                }
            }
        }
    }

    /**
     * Calcula el resumen administrativo de cada curso.
     * Pendiente: incorporar las métricas reales.
     */
    fun resumenPorCurso(): List<ResumenCurso> {

        return cursos.map {
            ResumenCurso(cursoId = it.id)
        }
    }
}
