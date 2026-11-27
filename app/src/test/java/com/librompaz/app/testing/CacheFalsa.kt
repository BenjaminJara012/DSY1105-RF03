package com.librompaz.app.testing

import com.librompaz.app.data.local.CacheLocal
import com.librompaz.app.data.local.InstantaneaServidor
import com.librompaz.app.model.Anotacion
import com.librompaz.app.model.Asignatura
import com.librompaz.app.model.Calificacion
import com.librompaz.app.model.CargaDocente
import com.librompaz.app.model.Curso
import com.librompaz.app.model.Estudiante
import com.librompaz.app.model.Evaluacion
import com.librompaz.app.model.ObjetivoAprendizaje
import com.librompaz.app.model.RegistroAsistencia
import com.librompaz.app.model.RegistroAuditoria
import com.librompaz.app.model.SesionClase
import com.librompaz.app.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * [CacheLocal] en memoria para probar LibroMpazApiRepository sin Room ni Android (ayuda ya entregada).
 *  - [usuariosGuardados]: usuarios "ya guardados" en el equipo (para probar el ingreso sin conexión).
 *  - [reemplazos] y [ultima]: cuántas veces y con qué datos se reemplazó la copia local.
 *  - [fallarAlReemplazar]: simula un fallo de Room.
 * Sus Flow se actualizan al reemplazar, como los de Room.
 */
class CacheFalsa(
    usuarios: List<Usuario> = emptyList(),
    asistenciasLocales: List<RegistroAsistencia> = emptyList()
) : CacheLocal {

    val usuariosGuardados = usuarios.toMutableList()
    var reemplazos = 0
    var ultima: InstantaneaServidor? = null
    var fallarAlReemplazar = false

    private val _cursos = MutableStateFlow<List<Curso>>(emptyList())
    private val _asignaturas = MutableStateFlow<List<Asignatura>>(emptyList())
    private val _estudiantes = MutableStateFlow<List<Estudiante>>(emptyList())
    private val _objetivos = MutableStateFlow<List<ObjetivoAprendizaje>>(emptyList())
    private val _cargas = MutableStateFlow<List<CargaDocente>>(emptyList())
    private val _asistencias = MutableStateFlow(asistenciasLocales)
    private val _sesiones = MutableStateFlow<List<SesionClase>>(emptyList())
    private val _evaluaciones = MutableStateFlow<List<Evaluacion>>(emptyList())
    private val _calificaciones = MutableStateFlow<List<Calificacion>>(emptyList())
    private val _anotaciones = MutableStateFlow<List<Anotacion>>(emptyList())
    private val _auditoria = MutableStateFlow<List<RegistroAuditoria>>(emptyList())

    override val cursos: Flow<List<Curso>> = _cursos
    override val asignaturas: Flow<List<Asignatura>> = _asignaturas
    override val estudiantes: Flow<List<Estudiante>> = _estudiantes
    override val objetivos: Flow<List<ObjetivoAprendizaje>> = _objetivos
    override val cargas: Flow<List<CargaDocente>> = _cargas
    override val asistencias: Flow<List<RegistroAsistencia>> = _asistencias
    override val sesiones: Flow<List<SesionClase>> = _sesiones
    override val evaluaciones: Flow<List<Evaluacion>> = _evaluaciones
    override val calificaciones: Flow<List<Calificacion>> = _calificaciones
    override val anotaciones: Flow<List<Anotacion>> = _anotaciones
    override val auditoria: Flow<List<RegistroAuditoria>> = _auditoria

    override fun cargasDe(docenteId: String): Flow<List<CargaDocente>> = MutableStateFlow(_cargas.value.filter { it.docenteId == docenteId })

    override suspend fun buscarUsuario(usuario: String): Usuario? =
        usuariosGuardados.firstOrNull { it.usuario.equals(usuario, ignoreCase = true) }

    override suspend fun guardarUsuario(usuario: Usuario) {
        usuariosGuardados.removeAll { it.id == usuario.id }
        usuariosGuardados += usuario
    }

    override suspend fun asistenciaDe(cursoId: String, fecha: String): List<RegistroAsistencia> =
        _asistencias.value.filter { it.cursoId == cursoId && it.fecha == fecha }

    override suspend fun reemplazar(datos: InstantaneaServidor) {
        if (fallarAlReemplazar) throw IllegalStateException("fallo de Room simulado")
        reemplazos += 1
        ultima = datos
        _cursos.value = datos.cursos
        _asignaturas.value = datos.asignaturas
        _estudiantes.value = datos.estudiantes
        _objetivos.value = datos.objetivos
        _cargas.value = datos.cargas
        _asistencias.value = datos.asistencias
        _sesiones.value = datos.sesiones
        _evaluaciones.value = datos.evaluaciones
        _calificaciones.value = datos.calificaciones
        _anotaciones.value = datos.anotaciones
        _auditoria.value = datos.auditoria
    }

    override suspend fun poblarSiVacia() = Unit
}
