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

/**
 * ViewModel único, compartido por todas las pantallas (se crea una sola vez en AppNavigation).
 *
 * ESTO ES UN ESQUELETO: las pantallas ya llaman a todas estas funciones, pero la lógica de
 * negocio del caso está vacía a propósito y marcada con TODO(equipo), citando el módulo de
 * Requerimientos_DSY1105-005 al que corresponde. Mientras no se implemente:
 *  - el login siempre rechaza (usar "Modo desarrollo" en la pantalla de login),
 *  - el docente ve todas las cargas (no solo las suyas),
 *  - guardar asistencia, clases, notas y anotaciones no hace nada,
 *  - las vistas de UTP, Inspectoría y Dirección no calculan métricas,
 *  - no se registra auditoría.
 *
 * Regla de negocio transversal: TODA creación o modificación debe dejar un RegistroAuditoria
 * (usuario, fecha y hora) automático e inalterable. Implementarla en cada función que guarde.
 */
class LibroMpazViewModel : ViewModel() {

    // =====================================================================
    // Módulo "Autenticación y alcance por rol"
    // =====================================================================

    private val _usuarioActivo = MutableStateFlow<Usuario?>(null)
    val usuarioActivo: StateFlow<Usuario?> = _usuarioActivo.asStateFlow()

    private val _errorLogin = MutableStateFlow<String?>(null)
    val errorLogin: StateFlow<String?> = _errorLogin.asStateFlow()

    /**
     * TODO(equipo): autenticar contra MockData.usuarios (o el backend que confirme el CITT).
     * Validar campos vacíos (patrón Guía 11). Si es correcto: dejar _usuarioActivo y devolver
     * true (AppNavigation ya lleva a cada perfil a su pantalla). Si no: _errorLogin y false.
     */
    fun login(usuario: String, clave: String): Boolean {
        _errorLogin.value = "Login sin implementar — ver TODO(equipo) en LibroMpazViewModel.login()."
        return false
    }

    /**
     * SOLO PARA DESARROLLO: entra con el primer usuario de prueba del perfil indicado, sin
     * validar credenciales. TODO(equipo): eliminar esta función y los botones "Modo desarrollo".
     */
    fun entrarModoDesarrollo(perfil: Perfil) {
        _usuarioActivo.value = MockData.usuarios.first { it.perfil == perfil }
        _errorLogin.value = null
    }

    fun logout() {
        _usuarioActivo.value = null
        _errorLogin.value = null
        _cursoSeleccionadoId.value = null
        _asignaturaSeleccionadaId.value = null
    }

    // =====================================================================
    // Datos de referencia y selección (no es lógica de negocio)
    // =====================================================================

    val cursos: List<Curso> = MockData.cursos
    val asignaturas: List<Asignatura> = MockData.asignaturas
    private val estudiantes: List<Estudiante> = MockData.estudiantes
    private val objetivos: List<ObjetivoAprendizaje> = MockData.objetivos

    private val _cursoSeleccionadoId = MutableStateFlow<String?>(null)
    val cursoSeleccionadoId: StateFlow<String?> = _cursoSeleccionadoId.asStateFlow()

    private val _asignaturaSeleccionadaId = MutableStateFlow<String?>(null)
    val asignaturaSeleccionadaId: StateFlow<String?> = _asignaturaSeleccionadaId.asStateFlow()

    fun seleccionarCurso(cursoId: String?, asignaturaId: String? = null) {
        _cursoSeleccionadoId.value = cursoId
        _asignaturaSeleccionadaId.value = asignaturaId
    }

    fun cursoPorId(id: String?): Curso? = cursos.find { it.id == id }
    fun asignaturaPorId(id: String?): Asignatura? = asignaturas.find { it.id == id }
    fun estudiantePorId(id: String?): Estudiante? = estudiantes.find { it.id == id }
    fun estudiantesDelCurso(cursoId: String?): List<Estudiante> = estudiantes.filter { it.cursoId == cursoId }
    fun objetivosDe(asignaturaId: String?): List<ObjetivoAprendizaje> = objetivos.filter { it.asignaturaId == asignaturaId }

    /**
     * TODO(equipo): devolver SOLO las cargas del docente autenticado.
     * RF: "mostrar únicamente los cursos/asignaturas bajo su asignación formal" (RBAC).
     * Hoy devuelve todas las cargas, incluso las de otros docentes.
     */
    fun misCargas(usuario: Usuario?): List<CargaDocente> = MockData.cargas

    // =====================================================================
    // Módulo "Asistencia"
    // =====================================================================

    private val _asistencias = MutableStateFlow(MockData.asistencias)

    /**
     * TODO(equipo): devolver la asistencia ya guardada del curso en esa fecha
     * (idEstudiante -> estado), para mostrarla y poder corregirla. Hoy devuelve un mapa vacío.
     * Pendiente con el CITT: si se puede corregir la asistencia de días anteriores.
     */
    fun asistenciaGuardada(cursoId: String, fecha: String): Map<String, EstadoAsistencia> = emptyMap()

    /**
     * TODO(equipo): guardar (o sobrescribir) la asistencia del curso en esa fecha, con
     * registradoPor = usuario activo y la fecha y hora actuales, y dejar un RegistroAuditoria.
     * RF: "registrar la asistencia diaria con estados presente, ausente, atrasado, justificado".
     * Hoy no hace nada.
     */
    fun guardarAsistencia(cursoId: String, fecha: String, estados: Map<String, EstadoAsistencia>) {
        // Sin implementar todavía.
    }

    /**
     * TODO(equipo): porcentaje de asistencia del curso (0 a 100) para Inspectoría y Dirección.
     * Hoy devuelve null (sin calcular).
     */
    fun porcentajeAsistencia(cursoId: String): Int? = null

    /**
     * TODO(equipo): estudiantes con inasistencias reiteradas (por ejemplo, 3 o más ausencias),
     * con su número de ausencias. RF de Inspectoría: "identificar inasistencias reiteradas".
     * Hoy devuelve una lista vacía.
     */
    fun inasistenciasReiteradas(): List<Pair<Estudiante, Int>> = emptyList()

    // =====================================================================
    // Módulo "Cobertura curricular"
    // =====================================================================

    private val _sesiones = MutableStateFlow(MockData.sesiones)
    val sesiones: StateFlow<List<SesionClase>> = _sesiones.asStateFlow()

    fun sesionesDe(cursoId: String?, asignaturaId: String?): List<SesionClase> =
        _sesiones.value.filter { it.cursoId == cursoId && it.asignaturaId == asignaturaId }

    /**
     * TODO(equipo): guardar la sesión con los OA y el contenido trabajados, con auditoría.
     * RF: "asociar OA y contenidos didácticos a cada sesión/bloque lectivo". Hoy no hace nada.
     */
    fun guardarSesion(cursoId: String, asignaturaId: String, fecha: String, bloque: String, oaIds: List<String>, contenido: String) {
        // Sin implementar todavía.
    }

    /**
     * TODO(equipo): ids de los OA ya trabajados en el curso y asignatura (según las sesiones
     * registradas). RF de UTP: "vistas analíticas de cobertura curricular por curso".
     * Hoy devuelve un conjunto vacío.
     */
    fun oasTrabajados(cursoId: String?, asignaturaId: String?): Set<String> = emptySet()

    // =====================================================================
    // Módulo "Calificaciones"
    // =====================================================================

    private val _evaluaciones = MutableStateFlow(MockData.evaluaciones)
    val evaluaciones: StateFlow<List<Evaluacion>> = _evaluaciones.asStateFlow()

    private val _calificaciones = MutableStateFlow(MockData.calificaciones)

    fun evaluacionesDe(cursoId: String?, asignaturaId: String?): List<Evaluacion> =
        _evaluaciones.value.filter { it.cursoId == cursoId && it.asignaturaId == asignaturaId }

    fun calificacionesDe(evaluacionId: String?): List<Calificacion> =
        _calificaciones.value.filter { it.evaluacionId == evaluacionId }

    /**
     * TODO(equipo): crear la evaluación (instrumento + fecha) para el curso y la asignatura, con
     * auditoría. Validar nombre y fecha obligatorios. Hoy no hace nada.
     */
    fun crearEvaluacion(cursoId: String, asignaturaId: String, nombreInstrumento: String, fecha: String) {
        // Sin implementar todavía.
    }

    /**
     * TODO(equipo): validar una nota escrita por el docente y devolver el mensaje de error o
     * null si es válida (número con coma o punto; rango 1,0 a 7,0 si el CITT lo confirma).
     * Hoy siempre devuelve null (sin validar).
     */
    fun validarNota(texto: String): String? = null

    /**
     * TODO(equipo): guardar las notas de la evaluación (idEstudiante -> nota escrita), con
     * auditoría. RF: "registrar calificaciones asociadas a un instrumento y fecha". Hoy no hace nada.
     */
    fun guardarCalificaciones(evaluacionId: String, notas: Map<String, String>) {
        // Sin implementar todavía.
    }

    /**
     * TODO(equipo): detectar desfases u omisiones de registro de notas, por ejemplo
     * evaluaciones con estudiantes sin nota, o cursos/asignaturas sin evaluaciones.
     * RF de UTP: "identificar desfases u omisiones de registro". Hoy devuelve una lista vacía.
     */
    fun desfasesCalificaciones(): List<String> = emptyList()

    // =====================================================================
    // Módulo "Hoja de vida"
    // =====================================================================

    private val _anotaciones = MutableStateFlow(MockData.anotaciones)
    val anotaciones: StateFlow<List<Anotacion>> = _anotaciones.asStateFlow()

    fun anotacionesDe(estudianteId: String?): List<Anotacion> =
        _anotaciones.value.filter { it.estudianteId == estudianteId }

    /**
     * TODO(equipo): registrar la anotación con marca temporal y "firma digital" del docente
     * (usuario activo + fecha y hora), y dejar auditoría. Hoy no hace nada.
     */
    fun registrarAnotacion(estudianteId: String, tipo: TipoAnotacion, descripcion: String) {
        // Sin implementar todavía.
    }

    /**
     * TODO(equipo): anotaciones que requieren intervención de Inspectoría (por ejemplo,
     * negativas reiteradas de un mismo estudiante). Hoy devuelve una lista vacía.
     */
    fun anotacionesQueRequierenIntervencion(): List<Anotacion> = emptyList()

    // =====================================================================
    // Módulo "Panel administrativo" (Dirección) y auditoría
    // =====================================================================

    private val _auditoria = MutableStateFlow(MockData.auditoria)
    val auditoria: StateFlow<List<RegistroAuditoria>> = _auditoria.asStateFlow()

    /**
     * TODO(equipo): calcular las métricas consolidadas de cada curso (asistencia, sesiones,
     * notas registradas, anotaciones negativas). RF de Dirección: "estado global administrativo
     * del colegio". Hoy devuelve los cursos sin métricas (valores null).
     */
    fun resumenPorCurso(): List<ResumenCurso> = cursos.map { ResumenCurso(cursoId = it.id) }
}
