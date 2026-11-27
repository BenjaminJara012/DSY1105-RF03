package com.librompaz.app.data.repository

import com.librompaz.app.data.local.CacheLocal
import com.librompaz.app.data.remote.ApiResult
import com.librompaz.app.data.remote.FuenteRemota
import com.librompaz.app.data.remote.TokenSesion
import com.librompaz.app.model.CargaDocente
import com.librompaz.app.model.EstadoAsistencia
import com.librompaz.app.model.Evaluacion
import com.librompaz.app.model.RegistroAsistencia
import com.librompaz.app.model.TipoAnotacion
import com.librompaz.app.model.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * REPOSITORY CON API (BuildConfig.USAR_API = true). Es la clase que completan en la Experiencia 3.
 *
 *  - La API (servidor local del aula) es la FUENTE DE VERDAD. Room es la caché y el respaldo sin conexión.
 *  - Lectura: API -> reemplaza la copia local en Room ([sincronizar]) -> la interfaz observa los Flow de Room.
 *  - Escritura: API primero; si el servidor la acepta, se vuelve a leer y se actualiza Room. Si no hay red,
 *    NO se guarda nada y se avisa con un mensaje claro: no hay cola de pendientes porque cada registro debe
 *    llevar la firma, la hora y la auditoría que pone el servidor (RN-02, RNF-04).
 *  - Las reglas de negocio las valida primero el ViewModel y luego el servidor; los mensajes del servidor
 *    llegan tal cual al usuario.
 *
 * [cache] es la copia local (Room en la app; una versión en memoria en las pruebas). [remota] habla con la
 * API. [sesion] guarda el token en memoria.
 */
class LibroMpazApiRepository(
    private val cache: CacheLocal,
    private val remota: FuenteRemota,
    private val sesion: TokenSesion
) : LibroMpazDatos {

    // ---------- Lecturas observables: salen de la copia local (ya resueltas) ----------

    override val cursos get() = cache.cursos
    override val asignaturas get() = cache.asignaturas
    override val estudiantes get() = cache.estudiantes
    override val objetivos get() = cache.objetivos
    override val cargas get() = cache.cargas
    override val asistencias get() = cache.asistencias
    override val sesiones get() = cache.sesiones
    override val evaluaciones get() = cache.evaluaciones
    override val calificaciones get() = cache.calificaciones
    override val anotaciones get() = cache.anotaciones
    override val auditoria get() = cache.auditoria

    /** RBAC (RF-01): SOLO las cargas del docente indicado. */
    override fun cargasDe(docenteId: String): Flow<List<CargaDocente>> = cache.cargasDe(docenteId)

    override suspend fun poblarSiVacia() = cache.poblarSiVacia()

    // ---------- Sesión (ya resuelto) ----------

    override fun retirarToken(): String? {
        val token = sesion.token
        sesion.token = null
        return token
    }

    /** Cierra la sesión en el servidor (mejor esfuerzo: si no hay red, el token vence solo). */
    override suspend fun cerrarSesionRemota(token: String?) {
        if (token.isNullOrEmpty()) return
        remota.cerrarSesion(token)
    }

    // ---------- TODO(E3-12 a E3-15) ----------

    // TODO(E3-12) [LibroMpazApiRepository.kt] Ingresar contra POST /auth/login (RF-01, RN-01, RNF-08).
    //   QUÉ: llamar a remota.iniciarSesion(correo, clave).
    //        - Exito: convertir el usuario con aEntidad(ClaveSegura.hash(clave)) (si el perfil no existe, devolver
    //          Rechazado("Correo o clave incorrectos")), guardar el token en sesion.token, guardar el usuario en la
    //          caché (cache.guardarUsuario) y devolver Aceptado(usuario).
    //        - Error (el servidor respondió): Rechazado(mensaje del servidor); no revela si el correo existe.
    //        - SinConexion: buscar al usuario en la caché (cache.buscarUsuario); si existe y ClaveSegura.coincide ->
    //          SinConexion(usuario, "<mensaje> Trabajas con los datos de la última sincronización."); si existe pero la clave
    //          no coincide -> Rechazado("Correo o clave incorrectos"); si no existe -> NoDisponible(mensaje).
    //   ACEPTACIÓN: docente.mat@libromp.cl con su clave deja el token en la sesión y el usuario en la caché; con servidor
    //        apagado solo ingresa quien ya ingresó antes en ese equipo; la clave NUNCA se guarda en claro.
    override suspend fun ingresar(correo: String, clave: String): ResultadoLogin = TODO("E3-12: ingresar contra la API")

    // TODO(E3-13) [LibroMpazApiRepository.kt] Sincronizar: API -> Room (lectura con la API como fuente de verdad).
    //   QUÉ: pedir a remota.descargarTodo(usuario.perfil); si es Exito, aplicar cache.reemplazar(instantánea) y
    //        devolver Exito(Unit); si falla la API devolver ese fallo SIN tocar Room; si Room lanza una excepción,
    //        devolver Error("No se pudo actualizar la copia local de los datos.") (relanzar la CancellationException).
    //   ACEPTACIÓN: tras sincronizar como Docente, los Flow de la caché emiten solo lo que ese perfil puede leer; una
    //        falla de red deja la caché como estaba; la prueba cuenta que reemplazar se llama una sola vez por sincronización.
    override suspend fun sincronizar(usuario: Usuario): ApiResult<Unit> = TODO("E3-13: sincronizar API -> Room")

    // TODO(E3-14) [LibroMpazApiRepository.kt] Escritura "API primero, luego Room".
    //   QUÉ: [escritura] es lo que respondió la API. Exito -> sincronizar(usuario) (si esa lectura falla, el guardado igual
    //        cuenta como hecho) y devolver Exito(Unit); Error -> devolverlo tal cual (mensaje del servidor); SinConexion ->
    //        SinConexion(mensaje + MENSAJE_NO_SE_GUARDO). Nunca se escribe en Room directamente.
    //   ACEPTACIÓN: un 409 del servidor no cambia la caché y el usuario ve el mensaje del servidor; sin red la caché no cambia y el
    //        mensaje termina en "No se guardó nada."; un éxito dispara exactamente una sincronización.
    private suspend fun <T> tras(escritura: ApiResult<T>, usuario: Usuario): ApiResult<Unit> = TODO("E3-14: escritura API primero")

    // TODO(E3-15) [LibroMpazApiRepository.kt] Asistencia de un día: API y, si falla, la copia local.
    //   QUÉ: pedir remota.asistenciaDe(cursoId, fecha); si es Exito devolver la lista; si es Error o SinConexion usar
    //        cache.asistenciaDe(cursoId, fecha) (comportamiento sin red: la pantalla sigue mostrando lo último conocido).
    //   ACEPTACIÓN: con servidor apagado devuelve lo que hay en la caché y no lanza excepción.
    override suspend fun asistenciaDe(cursoId: String, fecha: String): List<RegistroAsistencia> = TODO("E3-15: asistencia con respaldo local")

    // ---------- Escrituras: ya conectadas con FuenteRemota (E3-11) y con tras() (E3-14) ----------

    override suspend fun guardarAsistencia(
        usuario: Usuario,
        cursoId: String,
        fecha: String,
        estados: Map<String, EstadoAsistencia>,
        detalle: String
    ): ApiResult<Unit> = tras(remota.guardarAsistencia(cursoId, fecha, estados), usuario)

    override suspend fun guardarSesion(
        usuario: Usuario,
        cursoId: String,
        asignaturaId: String,
        fecha: String,
        bloque: String,
        oaIds: List<String>,
        contenido: String,
        detalle: String
    ): ApiResult<Unit> = tras(remota.crearSesion(cursoId, asignaturaId, fecha, bloque, oaIds, contenido), usuario)

    override suspend fun crearEvaluacion(
        usuario: Usuario,
        cursoId: String,
        asignaturaId: String,
        nombreInstrumento: String,
        fecha: String,
        detalle: String
    ): ApiResult<Unit> = tras(remota.crearEvaluacion(cursoId, asignaturaId, nombreInstrumento, fecha), usuario)

    override suspend fun eliminarEvaluacion(usuario: Usuario, evaluacion: Evaluacion, detalle: String): ApiResult<Unit> =
        tras(remota.eliminarEvaluacion(evaluacion.id), usuario)

    override suspend fun guardarCalificaciones(
        usuario: Usuario,
        evaluacionId: String,
        notas: Map<String, Double>,
        sinNota: List<String>,
        detalle: String
    ): ApiResult<Unit> = tras(remota.guardarCalificaciones(evaluacionId, notas, sinNota), usuario)

    override suspend fun registrarAnotacion(
        usuario: Usuario,
        estudianteId: String,
        tipo: TipoAnotacion,
        descripcion: String,
        detalle: String
    ): ApiResult<Unit> = tras(remota.crearAnotacion(estudianteId, tipo, descripcion), usuario)
}
