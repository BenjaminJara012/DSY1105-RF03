package com.librompaz.app.data.repository

import com.librompaz.app.data.remote.ApiResult
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

/**
 * SELECTOR DE FUENTE: la única [LibroMpazDatos] que ve el ViewModel. Elige entre el repositorio del
 * modo local ([local], LibroMpazRepository sobre Room) y el del modo servidor ([servidor],
 * LibroMpazApiRepository: API + caché) según la preferencia "modo local".
 *
 *  - Operaciones (login, escrituras, lecturas puntuales): la preferencia se lee en CADA llamada, así
 *    que un cambio vale desde la operación siguiente, sin reiniciar la app.
 *  - Lecturas observables (Flow): se reconectan solas a la otra fuente cuando cambia la preferencia.
 *  - En modo local nunca se toca [servidor]: no se arma ningún cliente HTTP ni se hace ninguna
 *    llamada de red (RN-01, RNF-07). [poblarSiVacia] sí prepara las dos bases (solo disco).
 *  - Los datos de las dos fuentes son independientes: lo creado en modo local no se sincroniza con el
 *    servidor al volver a ese modo.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SelectorFuente(
    private val preferencia: PreferenciaModo,
    private val local: LibroMpazDatos,
    private val servidor: LibroMpazDatos
) : LibroMpazDatos {

    /** La fuente que corresponde en este instante. */
    private fun activa(): LibroMpazDatos = if (preferencia.modoLocal) local else servidor

    private fun <T> observar(lectura: (LibroMpazDatos) -> Flow<T>): Flow<T> =
        preferencia.cambios.flatMapLatest { esLocal -> lectura(if (esLocal) local else servidor) }

    // ---------- Lecturas observables ----------
    override val cursos: Flow<List<Curso>> = observar { it.cursos }
    override val asignaturas: Flow<List<Asignatura>> = observar { it.asignaturas }
    override val estudiantes: Flow<List<Estudiante>> = observar { it.estudiantes }
    override val objetivos: Flow<List<ObjetivoAprendizaje>> = observar { it.objetivos }
    override val cargas: Flow<List<CargaDocente>> = observar { it.cargas }
    override val asistencias: Flow<List<RegistroAsistencia>> = observar { it.asistencias }
    override val sesiones: Flow<List<SesionClase>> = observar { it.sesiones }
    override val evaluaciones: Flow<List<Evaluacion>> = observar { it.evaluaciones }
    override val calificaciones: Flow<List<Calificacion>> = observar { it.calificaciones }
    override val anotaciones: Flow<List<Anotacion>> = observar { it.anotaciones }
    override val auditoria: Flow<List<RegistroAuditoria>> = observar { it.auditoria }

    override fun cargasDe(docenteId: String): Flow<List<CargaDocente>> = observar { it.cargasDe(docenteId) }

    /** Prepara las dos bases (cada una carga los datos de ejemplo si está vacía). */
    override suspend fun poblarSiVacia() {
        local.poblarSiVacia()
        servidor.poblarSiVacia()
    }

    // ---------- Sesión ----------
    override suspend fun ingresar(correo: String, clave: String): ResultadoLogin =
        activa().ingresar(correo, clave)

    override fun retirarToken(): String? = activa().retirarToken()

    override suspend fun cerrarSesionRemota(token: String?) = activa().cerrarSesionRemota(token)

    override suspend fun sincronizar(usuario: Usuario): ApiResult<Unit> = activa().sincronizar(usuario)

    override suspend fun asistenciaDe(cursoId: String, fecha: String): List<RegistroAsistencia> =
        activa().asistenciaDe(cursoId, fecha)

    // ---------- Escrituras ----------
    override suspend fun guardarAsistencia(
        usuario: Usuario,
        cursoId: String,
        fecha: String,
        estados: Map<String, EstadoAsistencia>,
        detalle: String
    ): ApiResult<Unit> = activa().guardarAsistencia(usuario, cursoId, fecha, estados, detalle)

    override suspend fun guardarSesion(
        usuario: Usuario,
        cursoId: String,
        asignaturaId: String,
        fecha: String,
        bloque: String,
        oaIds: List<String>,
        contenido: String,
        detalle: String
    ): ApiResult<Unit> = activa().guardarSesion(usuario, cursoId, asignaturaId, fecha, bloque, oaIds, contenido, detalle)

    override suspend fun crearEvaluacion(
        usuario: Usuario,
        cursoId: String,
        asignaturaId: String,
        nombreInstrumento: String,
        fecha: String,
        detalle: String
    ): ApiResult<Unit> = activa().crearEvaluacion(usuario, cursoId, asignaturaId, nombreInstrumento, fecha, detalle)

    override suspend fun eliminarEvaluacion(usuario: Usuario, evaluacion: Evaluacion, detalle: String): ApiResult<Unit> =
        activa().eliminarEvaluacion(usuario, evaluacion, detalle)

    override suspend fun guardarCalificaciones(
        usuario: Usuario,
        evaluacionId: String,
        notas: Map<String, Double>,
        sinNota: List<String>,
        detalle: String
    ): ApiResult<Unit> = activa().guardarCalificaciones(usuario, evaluacionId, notas, sinNota, detalle)

    override suspend fun registrarAnotacion(
        usuario: Usuario,
        estudianteId: String,
        tipo: TipoAnotacion,
        descripcion: String,
        detalle: String
    ): ApiResult<Unit> = activa().registrarAnotacion(usuario, estudianteId, tipo, descripcion, detalle)

    /** Siempre actúa sobre la base local, esté el modo que esté: nunca toca lo del servidor. */
    override suspend fun restablecerDatosLocales() = local.restablecerDatosLocales()
}
