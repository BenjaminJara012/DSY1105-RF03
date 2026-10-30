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
import com.librompaz.app.util.validarFechaAsistencia

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
     * Indica si se está consultando o guardando asistencia.
     * La interfaz puede utilizarlo para mostrar un indicador de carga.
     */
    private val _cargandoAsistencia = MutableStateFlow(false)
    val cargandoAsistencia: StateFlow<Boolean> =
        _cargandoAsistencia.asStateFlow()

    /**
     * Contiene el último error producido en el módulo de asistencia.
     * Si es null, no hay ningún error pendiente.
     */
    private val _errorAsistencia = MutableStateFlow<String?>(null)
    val errorAsistencia: StateFlow<String?> =
        _errorAsistencia.asStateFlow()

    /**
     * Contiene un mensaje de confirmación cuando la operación termina.
     */
    private val _mensajeAsistencia = MutableStateFlow<String?>(null)
    val mensajeAsistencia: StateFlow<String?> =
        _mensajeAsistencia.asStateFlow()


    /**
     * Limpia los mensajes del módulo de asistencia.
     * Evita mostrar errores o confirmaciones de operaciones anteriores.
     */
    fun limpiarMensajesAsistencia() {
        _errorAsistencia.value = null
        _mensajeAsistencia.value = null
    }

    /**
     * Verifica que el usuario activo sea un docente
     * y tenga asignado el curso solicitado.
     *
     * @param cursoId Identificador del curso.
     * @return true si está autorizado; false si no.
     */
    fun puedeRegistrarAsistencia(cursoId: String): Boolean {

        // Obtiene el usuario que inició sesión.
        val usuario = _usuarioActivo.value ?: return false

        // Solo los docentes pueden registrar asistencia.
        if (usuario.perfil != Perfil.DOCENTE) {
            return false
        }

        // Comprueba que el docente tenga asignado el curso.
        return MockData.cargas.any { carga ->
            carga.docenteId == usuario.id &&
                    carga.cursoId == cursoId
        }
    }

    /**
     * Verifica que los estados recibidos correspondan
     * exactamente a los estudiantes del curso.
     *
     * Evita guardar estudiantes de otros cursos
     * o registros incompletos.
     */
    fun validarEstudiantesAsistencia(
        cursoId: String,
        estados: Map<String, EstadoAsistencia>
    ): Boolean {

        // Obtiene los identificadores de los estudiantes del curso.
        val estudiantesEsperados = estudiantesDelCurso(cursoId)
            .map { it.id }
            .toSet()

        // No permite registrar asistencia en cursos sin estudiantes.
        if (estudiantesEsperados.isEmpty()) {
            return false
        }

        // Los estudiantes recibidos deben coincidir exactamente.
        return estados.keys == estudiantesEsperados
    }

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

    /**
     * Valida los datos antes de registrar asistencia.
     *
     * El guardado definitivo se conectará al Repository
     * cuando esté disponible la implementación de Room.
     */
    fun guardarAsistencia(
        cursoId: String,
        fecha: String,
        estados: Map<String, EstadoAsistencia>
    ) {

        // Limpia los mensajes de operaciones anteriores.
        limpiarMensajesAsistencia()

        // Comprueba que la fecha sea válida y no futura.
        if (!validarFechaAsistencia(fecha)) {
            _errorAsistencia.value = "La fecha ingresada no es válida."
            return
        }

        // Comprueba que el docente tenga asignado el curso.
        if (!puedeRegistrarAsistencia(cursoId)) {
            _errorAsistencia.value =
                "No tienes autorización para registrar asistencia en este curso."
            return
        }

        // Comprueba que los estudiantes pertenezcan al curso.
        if (!validarEstudiantesAsistencia(cursoId, estados)) {
            _errorAsistencia.value =
                "Los estudiantes no corresponden al curso seleccionado."
            return
        }

        // Pendiente: conectar el guardado con el Repository de Room.
        _errorAsistencia.value =
            "Las validaciones fueron aprobadas, pero el guardado aún no está disponible."
    }


    /**
     * Construye la descripción de la operación de asistencia
     * que se enviará al Repository para registrar la auditoría.
     */

    /**
     * Construye el detalle para la auditoría de asistencia.
     * Incluye curso, fecha y asignaturas asignadas al docente.
     */
    fun construirDetalleAsistencia(
        cursoId: String,
        fecha: String,
        usuario: Usuario
    ): String {

        // Obtiene el nombre del curso.
        val nombreCurso = cursoPorId(cursoId)?.nombre ?: cursoId

        // Busca las asignaturas del docente en ese curso.
        val nombresAsignaturas = MockData.cargas
            .filter { carga ->
                carga.docenteId == usuario.id &&
                        carga.cursoId == cursoId
            }
            .mapNotNull { carga ->
                asignaturaPorId(carga.asignaturaId)?.nombre
            }
            .distinct()

        // Une las asignaturas, si existen.
        val asignaturas = nombresAsignaturas.joinToString(", ")

        return buildString {
            append("Asistencia $nombreCurso")

            if (asignaturas.isNotEmpty()) {
                append(", $asignaturas")
            }

            append(", $fecha")
        }
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
