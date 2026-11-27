package com.librompaz.app.data.local

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

/**
 * Lo que entregó el servidor en una sincronización, ya convertido a entidades. Las colecciones a
 * las que el perfil no tiene acceso llegan vacías: así la copia local nunca conserva datos de una
 * sesión anterior que este perfil no debe ver.
 *
 * [anotaciones] y [auditoria] vienen del servidor de la más reciente a la más antigua.
 */
data class InstantaneaServidor(
    val cursos: List<Curso> = emptyList(),
    val asignaturas: List<Asignatura> = emptyList(),
    val estudiantes: List<Estudiante> = emptyList(),
    val objetivos: List<ObjetivoAprendizaje> = emptyList(),
    val cargas: List<CargaDocente> = emptyList(),
    val asistencias: List<RegistroAsistencia> = emptyList(),
    val sesiones: List<SesionClase> = emptyList(),
    val evaluaciones: List<Evaluacion> = emptyList(),
    val calificaciones: List<Calificacion> = emptyList(),
    val anotaciones: List<Anotacion> = emptyList(),
    val auditoria: List<RegistroAuditoria> = emptyList()
)

/**
 * Copia local (caché y respaldo sin conexión) de lo que sirve la API. La implementación real es
 * [CacheRoom]; las pruebas usan una versión en memoria. Las pantallas observan estos Flow: cuando la
 * sincronización reemplaza los datos, la interfaz se actualiza sola.
 */
interface CacheLocal {
    val cursos: Flow<List<Curso>>
    val asignaturas: Flow<List<Asignatura>>
    val estudiantes: Flow<List<Estudiante>>
    val objetivos: Flow<List<ObjetivoAprendizaje>>
    val cargas: Flow<List<CargaDocente>>
    val asistencias: Flow<List<RegistroAsistencia>>
    val sesiones: Flow<List<SesionClase>>
    val evaluaciones: Flow<List<Evaluacion>>
    val calificaciones: Flow<List<Calificacion>>
    val anotaciones: Flow<List<Anotacion>>
    val auditoria: Flow<List<RegistroAuditoria>>

    fun cargasDe(docenteId: String): Flow<List<CargaDocente>>

    suspend fun buscarUsuario(usuario: String): Usuario?
    suspend fun guardarUsuario(usuario: Usuario)
    suspend fun asistenciaDe(cursoId: String, fecha: String): List<RegistroAsistencia>

    /** Reemplaza TODA la copia local por [datos], de forma atómica (o se aplica completa o no cambia nada). */
    suspend fun reemplazar(datos: InstantaneaServidor)

    /** Primera ejecución sin servidor: carga los datos de ejemplo (los mismos de la semilla de la API). */
    suspend fun poblarSiVacia()
}
