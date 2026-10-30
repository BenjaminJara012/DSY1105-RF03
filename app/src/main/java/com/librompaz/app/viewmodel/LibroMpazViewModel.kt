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
import com.librompaz.app.data.AsistenciaRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

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
class LibroMpazViewModel(
    private val asistenciaRepository: AsistenciaRepository? = null
) : ViewModel() {

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

    // Indica si hay una operación de asistencia en curso.
    private val _cargandoAsistencia = MutableStateFlow(false)
    val cargandoAsistencia: StateFlow<Boolean> =
        _cargandoAsistencia.asStateFlow()

    // Comunica errores a la interfaz.
    private val _errorAsistencia = MutableStateFlow<String?>(null)
    val errorAsistencia: StateFlow<String?> =
        _errorAsistencia.asStateFlow()

    // Comunica confirmaciones a la interfaz.
    private val _mensajeAsistencia = MutableStateFlow<String?>(null)
    val mensajeAsistencia: StateFlow<String?> =
        _mensajeAsistencia.asStateFlow()

    // Estados recuperados desde Room.
    private val _estadosAsistenciaGuardada =
        MutableStateFlow<Map<String, EstadoAsistencia>>(emptyMap())

    val estadosAsistenciaGuardada: StateFlow<Map<String, EstadoAsistencia>> =
        _estadosAsistenciaGuardada.asStateFlow()

    // Permite descartar resultados de consultas anteriores.
    private var ultimaConsultaAsistencia = 0L

    // Evita consultas y guardados simultáneos.
    private var guardandoAsistencia = false

    /**
     * Limpia los mensajes de operaciones anteriores.
     */
    fun limpiarMensajesAsistencia() {
        _errorAsistencia.value = null
        _mensajeAsistencia.value = null
    }

    /**
     * Comprueba que el usuario sea docente
     * y tenga asignado el curso.
     */
    fun puedeRegistrarAsistencia(cursoId: String): Boolean {
        val usuario = _usuarioActivo.value ?: return false

        if (usuario.perfil != Perfil.DOCENTE) {
            return false
        }

        return MockData.cargas.any { carga ->
            carga.docenteId == usuario.id &&
                    carga.cursoId == cursoId
        }
    }

    /**
     * Comprueba que los estados correspondan
     * exactamente a los estudiantes del curso.
     */
    fun validarEstudiantesAsistencia(
        cursoId: String,
        estados: Map<String, EstadoAsistencia>
    ): Boolean {
        val estudiantesEsperados = estudiantesDelCurso(cursoId)
            .map { it.id }
            .toSet()

        if (estudiantesEsperados.isEmpty()) {
            return false
        }

        return estados.keys == estudiantesEsperados
    }

    /**
     * Compatibilidad temporal con la pantalla original.
     * La interfaz nueva utilizará cargarAsistencia()
     * y estadosAsistenciaGuardada.
     */
    fun asistenciaGuardada(
        cursoId: String,
        fecha: String
    ): Map<String, EstadoAsistencia> = emptyMap()

    /**
     * Consulta la asistencia almacenada en Room.
     * Descarta resultados de consultas anteriores.
     */
    fun cargarAsistencia(cursoId: String, fecha: String) {

        // No permite consultar mientras se guarda.
        if (guardandoAsistencia) {
            _errorAsistencia.value =
                "Espera a que termine el guardado."
            return
        }

        val consultaActual = ++ultimaConsultaAsistencia

        limpiarMensajesAsistencia()
        _estadosAsistenciaGuardada.value = emptyMap()

        if (!validarFechaAsistencia(fecha)) {
            _errorAsistencia.value =
                "La fecha ingresada no es válida."
            _cargandoAsistencia.value = false
            return
        }

        if (cursoId.isBlank()) {
            _errorAsistencia.value =
                "Debes seleccionar un curso."
            _cargandoAsistencia.value = false
            return
        }

        _cargandoAsistencia.value = true

        viewModelScope.launch {
            try {
                val repository = checkNotNull(asistenciaRepository) {
                    "El Repository de asistencia no fue inicializado."
                }

                val registros = repository.obtenerAsistencia(
                    cursoId,
                    fecha
                )

                // Solo publica la consulta más reciente.
                if (consultaActual == ultimaConsultaAsistencia) {
                    _estadosAsistenciaGuardada.value =
                        registros.associate { registro ->
                            registro.estudianteId to registro.estado
                        }
                }

            } catch (e: Exception) {
                if (consultaActual == ultimaConsultaAsistencia) {
                    _errorAsistencia.value =
                        "No se pudo cargar la asistencia."
                }
            } finally {
                if (consultaActual == ultimaConsultaAsistencia) {
                    _cargandoAsistencia.value = false
                }
            }
        }
    }

    /**
     * Valida y guarda asistencia junto con su auditoría.
     * Publica el resultado mediante StateFlow.
     */
    fun guardarAsistencia(
        cursoId: String,
        fecha: String,
        estados: Map<String, EstadoAsistencia>
    ) {
        // Evita operaciones simultáneas.
        if (guardandoAsistencia || _cargandoAsistencia.value) {
            return
        }

        limpiarMensajesAsistencia()

        if (!validarFechaAsistencia(fecha)) {
            _errorAsistencia.value =
                "La fecha ingresada no es válida."
            return
        }

        if (!puedeRegistrarAsistencia(cursoId)) {
            _errorAsistencia.value =
                "No tienes autorización para registrar asistencia en este curso."
            return
        }

        if (!validarEstudiantesAsistencia(cursoId, estados)) {
            _errorAsistencia.value =
                "Los estudiantes no corresponden al curso seleccionado."
            return
        }

        val usuario = _usuarioActivo.value ?: run {
            _errorAsistencia.value = "Debes iniciar sesión."
            return
        }

        val detalle = construirDetalleAsistencia(
            cursoId,
            fecha,
            usuario
        )

        // Guarda qué consulta estaba activa.
        val consultaAlGuardar = ultimaConsultaAsistencia

        // Bloquea inmediatamente nuevas operaciones.
        guardandoAsistencia = true
        _cargandoAsistencia.value = true

        viewModelScope.launch {
            try {
                val repository = checkNotNull(asistenciaRepository) {
                    "El Repository de asistencia no fue inicializado."
                }

                // El Repository guarda registros y auditoría.
                val accion = repository.guardarAsistencia(
                    cursoId = cursoId,
                    fecha = fecha,
                    estados = estados,
                    usuario = usuario,
                    detalle = detalle
                )

                // Actualiza los estados solo si la consulta
                // vigente no ha cambiado.
                if (consultaAlGuardar == ultimaConsultaAsistencia) {
                    _estadosAsistenciaGuardada.value = estados
                }

                _mensajeAsistencia.value = when (accion) {
                    "CREAR" ->
                        "Asistencia registrada correctamente."
                    "MODIFICAR" ->
                        "Asistencia actualizada correctamente."
                    else ->
                        "Operación de asistencia completada."
                }

            } catch (e: Exception) {
                _errorAsistencia.value =
                    "No se pudo guardar la asistencia. Inténtalo nuevamente."
            } finally {
                guardandoAsistencia = false
                _cargandoAsistencia.value = false
            }
        }
    }

    /**
     * Construye el detalle de auditoría con
     * curso, asignaturas y fecha.
     */
    fun construirDetalleAsistencia(
        cursoId: String,
        fecha: String,
        usuario: Usuario
    ): String {
        val nombreCurso = cursoPorId(cursoId)?.nombre ?: cursoId

        val nombresAsignaturas = MockData.cargas
            .filter { carga ->
                carga.docenteId == usuario.id &&
                        carga.cursoId == cursoId
            }
            .mapNotNull { carga ->
                asignaturaPorId(carga.asignaturaId)?.nombre
            }
            .distinct()

        val asignaturas = nombresAsignaturas.joinToString(", ")

        return buildString {
            append("Asistencia $nombreCurso")

            if (asignaturas.isNotEmpty()) {
                append(", $asignaturas")
            }

            append(", $fecha")
        }
    }

    // Pendiente para los módulos de Inspectoría y Dirección.
    fun porcentajeAsistencia(cursoId: String): Int? = null

    fun inasistenciasReiteradas(): List<Pair<Estudiante, Int>> =
        emptyList()


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
