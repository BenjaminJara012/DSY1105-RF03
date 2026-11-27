package com.librompaz.app.data.repository

import com.librompaz.app.data.remote.ApiResult
import com.librompaz.app.domain.ClaveSegura
import com.librompaz.app.model.Anotacion
import com.librompaz.app.model.Asignatura
import com.librompaz.app.model.Calificacion
import com.librompaz.app.model.CargaDocente
import com.librompaz.app.model.Curso
import com.librompaz.app.model.EstadoAsistencia
import com.librompaz.app.model.Estudiante
import com.librompaz.app.model.Evaluacion
import com.librompaz.app.model.ObjetivoAprendizaje
import com.librompaz.app.model.RegistroAsistencia
import com.librompaz.app.model.RegistroAuditoria
import com.librompaz.app.model.SesionClase
import com.librompaz.app.model.TipoAnotacion
import com.librompaz.app.model.Usuario
import kotlinx.coroutines.flow.Flow

/** Resultado de ingresar. */
sealed class ResultadoLogin {
    /** Credenciales aceptadas (por la base local en modo local, o por el servidor en modo API). */
    data class Aceptado(val usuario: Usuario) : ResultadoLogin()

    /** Solo modo API: el servidor no respondió, pero las credenciales coinciden con las de un ingreso anterior en este equipo. */
    data class SinConexion(val usuario: Usuario, val aviso: String) : ResultadoLogin()

    /** Credenciales incorrectas u otra respuesta del servidor; [mensaje] está listo para el usuario. */
    data class Rechazado(val mensaje: String) : ResultadoLogin()

    /** Solo modo API: sin conexión y sin forma de validar el ingreso en este equipo. */
    data class NoDisponible(val mensaje: String) : ResultadoLogin()
}

const val MENSAJE_CREDENCIALES = "Correo o clave incorrectos"

/**
 * RF-01, RN-01 y RNF-08 en modo local: acepta al usuario solo si existe y la clave escrita coincide con
 * su hash (SHA-256 con la misma sal del servidor). Un correo inexistente y una clave mala dan el MISMO
 * mensaje: no se revela qué correos existen. Función pura, probada con JUnit.
 */
fun verificarCredenciales(usuario: Usuario?, clave: String): ResultadoLogin =
    if (usuario != null && ClaveSegura.coincide(clave, usuario.claveHash)) {
        ResultadoLogin.Aceptado(usuario)
    } else {
        ResultadoLogin.Rechazado(MENSAJE_CREDENCIALES)
    }

/**
 * CONTRATO DE DATOS que usa el ViewModel. El ViewModel no sabe de dónde vienen los datos: hay dos
 * implementaciones y el [SelectorFuente] elige una en CADA operación según la preferencia "modo local"
 * (cuyo valor inicial sale de BuildConfig.USAR_API: false = local, true = servidor).
 *
 *  - [LibroMpazRepository] (modo local): todo en Room, sin red. Ya está completa.
 *  - [LibroMpazApiRepository] (modo servidor): la API es la fuente de verdad y Room la caché.
 *    Es la que completan en la Experiencia 3 (TODO E3-12 a E3-15).
 *
 * Las lecturas son Flow de Room en ambos casos: la interfaz se actualiza sola cuando cambia la tabla.
 * Las escrituras devuelven [ApiResult]: en modo local siempre Exito (o lanzan si Room falla); en modo API
 * pueden ser Error (regla de negocio o permiso) o SinConexion. El parámetro [detalle] es el texto de
 * auditoría que arma el ViewModel: solo lo usa el modo local, porque en modo API el servidor audita.
 */
interface LibroMpazDatos {

    // ---------- Lecturas observables ----------
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

    /** RBAC (RF-01): SOLO las cargas del docente indicado. */
    fun cargasDe(docenteId: String): Flow<List<CargaDocente>>

    /** La primera vez que se abre la app (base vacía) inserta los datos ficticios de MockData. */
    suspend fun poblarSiVacia()

    // ---------- Sesión (RF-01, RN-01, RNF-08) ----------
    suspend fun ingresar(correo: String, clave: String): ResultadoLogin

    /** Olvida el token de inmediato y lo devuelve (null si no había), para poder avisar al servidor después. */
    fun retirarToken(): String?

    /** Cierra la sesión en el servidor (mejor esfuerzo). En modo local no hace nada. */
    suspend fun cerrarSesionRemota(token: String?)

    // ---------- Sincronización y lecturas puntuales ----------

    /** API -> Room: reemplaza la copia local por lo que el perfil puede leer. En modo local no hace nada y devuelve Exito. */
    suspend fun sincronizar(usuario: Usuario): ApiResult<Unit>

    /** Asistencia ya guardada del curso en esa fecha (para mostrarla y poder corregirla). */
    suspend fun asistenciaDe(cursoId: String, fecha: String): List<RegistroAsistencia>

    // ---------- Escrituras ----------
    suspend fun guardarAsistencia(
        usuario: Usuario,
        cursoId: String,
        fecha: String,
        estados: Map<String, EstadoAsistencia>,
        detalle: String
    ): ApiResult<Unit>

    suspend fun guardarSesion(
        usuario: Usuario,
        cursoId: String,
        asignaturaId: String,
        fecha: String,
        bloque: String,
        oaIds: List<String>,
        contenido: String,
        detalle: String
    ): ApiResult<Unit>

    suspend fun crearEvaluacion(
        usuario: Usuario,
        cursoId: String,
        asignaturaId: String,
        nombreInstrumento: String,
        fecha: String,
        detalle: String
    ): ApiResult<Unit>

    suspend fun eliminarEvaluacion(usuario: Usuario, evaluacion: Evaluacion, detalle: String): ApiResult<Unit>

    suspend fun guardarCalificaciones(
        usuario: Usuario,
        evaluacionId: String,
        notas: Map<String, Double>,
        sinNota: List<String>,
        detalle: String
    ): ApiResult<Unit>

    suspend fun registrarAnotacion(
        usuario: Usuario,
        estudianteId: String,
        tipo: TipoAnotacion,
        descripcion: String,
        detalle: String
    ): ApiResult<Unit>

    // ---------- Modo local ----------

    /**
     * Borra lo guardado en modo local y vuelve a cargar los datos de ejemplo. No toca los datos del
     * servidor. Solo tiene efecto en [LibroMpazRepository]; la versión con API no hace nada.
     */
    suspend fun restablecerDatosLocales() {}
}
