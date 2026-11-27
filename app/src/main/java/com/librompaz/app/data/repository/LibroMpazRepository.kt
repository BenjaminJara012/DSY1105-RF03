package com.librompaz.app.data.repository

import androidx.room.withTransaction
import com.librompaz.app.data.MockData
import com.librompaz.app.data.local.AppDatabase
import com.librompaz.app.data.remote.ApiResult
import com.librompaz.app.domain.construirRegistrosAsistencia
import com.librompaz.app.domain.idCalificacion
import com.librompaz.app.model.Anotacion
import com.librompaz.app.model.Calificacion
import com.librompaz.app.model.CargaDocente
import com.librompaz.app.model.EstadoAsistencia
import com.librompaz.app.model.Evaluacion
import com.librompaz.app.model.RegistroAsistencia
import com.librompaz.app.model.RegistroAuditoria
import com.librompaz.app.model.SesionClase
import com.librompaz.app.model.TipoAnotacion
import com.librompaz.app.model.Usuario
import com.librompaz.app.util.ahora
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * REPOSITORY LOCAL (modo local): todo se guarda en Room, sin red. Está completo y es el que usa la app
 * mientras el modo local esté activo (valor inicial: BuildConfig.USAR_API = false; se cambia en la app, en
 * el cuadro "Servidor"). No hay que modificarlo en la Experiencia 3: la integración con la API es la clase
 * [LibroMpazApiRepository]. Trabaja sobre su propia base ("libro_mpaz_local.db"), independiente de la copia
 * del servidor.
 *
 * El ViewModel no sabe si los datos vienen de Room o de una API REST (solo conoce [LibroMpazDatos]).
 *
 * RN-02 (auditoría automática e inalterable): cada función que crea o modifica algo registra su
 * auditoría DENTRO de la misma transacción (db.withTransaction). Así, o se guardan las dos cosas
 * o ninguna: no puede quedar un cambio sin auditar. El repositorio no ofrece funciones para
 * editar ni borrar anotaciones ni auditoría (RN-06, RN-02).
 */
class LibroMpazRepository(private val db: AppDatabase) : LibroMpazDatos {

    private val usuarioDao = db.usuarioDao()
    private val academicoDao = db.academicoDao()
    private val asistenciaDao = db.asistenciaDao()
    private val sesionDao = db.sesionDao()
    private val calificacionDao = db.calificacionDao()
    private val anotacionDao = db.anotacionDao()
    private val auditoriaDao = db.auditoriaDao()

    // ---------- Lecturas observables (Flow: se actualizan solas cuando cambia la tabla) ----------

    override val cursos = academicoDao.cursos()
    override val asignaturas = academicoDao.asignaturas()
    override val estudiantes = academicoDao.estudiantes()
    override val objetivos = academicoDao.objetivos()
    override val cargas = academicoDao.cargas()
    override val asistencias = asistenciaDao.todas()
    override val sesiones = sesionDao.todas()
    override val evaluaciones = calificacionDao.evaluaciones()
    override val calificaciones = calificacionDao.calificaciones()
    override val anotaciones = anotacionDao.todas()
    override val auditoria = auditoriaDao.todos()

    /** RBAC (RF-01): SOLO las cargas del docente indicado (la consulta filtra por docenteId en SQL). */
    override fun cargasDe(docenteId: String): Flow<List<CargaDocente>> = academicoDao.cargasDe(docenteId)

    // ---------- Sesión ----------

    /**
     * Busca al usuario en Room y compara la clave por su hash (RF-01). La decisión la toma la función pura
     * [verificarCredenciales], con mensaje genérico a propósito: no revela si el correo existe (RN-01).
     */
    override suspend fun ingresar(correo: String, clave: String): ResultadoLogin =
        verificarCredenciales(usuarioDao.buscarPorUsuario(correo.trim()), clave)

    /** Modo local: no hay token. */
    override fun retirarToken(): String? = null

    /** Modo local: no hay servidor al que avisar. */
    override suspend fun cerrarSesionRemota(token: String?) = Unit

    /** Modo local: no hay nada que sincronizar. */
    override suspend fun sincronizar(usuario: Usuario): ApiResult<Unit> = ApiResult.Exito(Unit)

    // ---------- Lecturas puntuales (suspend) ----------

    override suspend fun asistenciaDe(cursoId: String, fecha: String): List<RegistroAsistencia> =
        asistenciaDao.delCursoEnFecha(cursoId, fecha)

    /** Borra TODO lo guardado en esta base (la local) y vuelve a cargar los datos ficticios de MockData. */
    override suspend fun restablecerDatosLocales() {
        // clearAllTables no se puede llamar dentro de una transacción ni en el hilo principal.
        withContext(Dispatchers.IO) { db.clearAllTables() }
        poblarSiVacia()
    }

    /** La primera vez que se abre la app (base vacía) inserta los datos ficticios de MockData. */
    override suspend fun poblarSiVacia() {
        if (usuarioDao.contar() > 0) return
        db.withTransaction {
            // El orden importa por las claves foráneas: primero las tablas "padre".
            usuarioDao.insertarTodos(MockData.usuarios)
            academicoDao.insertarCursos(MockData.cursos)
            academicoDao.insertarAsignaturas(MockData.asignaturas)
            academicoDao.insertarEstudiantes(MockData.estudiantes)
            academicoDao.insertarObjetivos(MockData.objetivos)
            academicoDao.insertarCargas(MockData.cargas)
            asistenciaDao.guardar(MockData.asistencias)
            sesionDao.insertarTodas(MockData.sesiones)
            calificacionDao.insertarEvaluaciones(MockData.evaluaciones)
            calificacionDao.guardarNotas(MockData.calificaciones)
            anotacionDao.insertarTodas(MockData.anotaciones)
            auditoriaDao.insertarTodos(MockData.auditoria)
        }
    }

    // ---------- Escrituras (todas con auditoría en la misma transacción) ----------

    /**
     * Guarda o reemplaza la asistencia del curso en esa fecha (RN-05) en una sola transacción.
     * La auditoría dice MODIFICAR si ya existía un registro ese día, y CREAR si era nuevo.
     */
    override suspend fun guardarAsistencia(
        usuario: Usuario,
        cursoId: String,
        fecha: String,
        estados: Map<String, EstadoAsistencia>,
        detalle: String
    ): ApiResult<Unit> {
        db.withTransaction {
            val yaExistia = asistenciaDao.delCursoEnFecha(cursoId, fecha).isNotEmpty()
            asistenciaDao.guardar(construirRegistrosAsistencia(cursoId, fecha, estados, usuario.nombre, ahora()))
            auditar(usuario, if (yaExistia) "MODIFICAR" else "CREAR", detalle)
        }
        return ApiResult.Exito(Unit)
    }

    override suspend fun guardarSesion(
        usuario: Usuario,
        cursoId: String,
        asignaturaId: String,
        fecha: String,
        bloque: String,
        oaIds: List<String>,
        contenido: String,
        detalle: String
    ): ApiResult<Unit> {
        db.withTransaction {
            sesionDao.insertar(
                SesionClase(nuevoId(), cursoId, asignaturaId, fecha, bloque, oaIds, contenido, usuario.nombre, ahora())
            )
            auditar(usuario, "CREAR", detalle)
        }
        return ApiResult.Exito(Unit)
    }

    override suspend fun crearEvaluacion(
        usuario: Usuario,
        cursoId: String,
        asignaturaId: String,
        nombreInstrumento: String,
        fecha: String,
        detalle: String
    ): ApiResult<Unit> {
        db.withTransaction {
            calificacionDao.insertarEvaluacion(Evaluacion(nuevoId(), cursoId, asignaturaId, nombreInstrumento, fecha))
            auditar(usuario, "CREAR", detalle)
        }
        return ApiResult.Exito(Unit)
    }

    /** Elimina la evaluación; sus notas se borran por CASCADE. */
    override suspend fun eliminarEvaluacion(usuario: Usuario, evaluacion: Evaluacion, detalle: String): ApiResult<Unit> {
        db.withTransaction {
            calificacionDao.eliminarEvaluacion(evaluacion)
            auditar(usuario, "ELIMINAR", detalle)
        }
        return ApiResult.Exito(Unit)
    }

    /**
     * Guarda las notas (estudianteId -> nota). Los estudiantes en [sinNota] tenían nota y el
     * docente la dejó en blanco: esa nota se elimina.
     */
    override suspend fun guardarCalificaciones(
        usuario: Usuario,
        evaluacionId: String,
        notas: Map<String, Double>,
        sinNota: List<String>,
        detalle: String
    ): ApiResult<Unit> {
        db.withTransaction {
            val habiaNotas = calificacionDao.notasDe(evaluacionId).isNotEmpty()
            calificacionDao.guardarNotas(
                notas.map { (estudianteId, nota) ->
                    Calificacion(idCalificacion(evaluacionId, estudianteId), evaluacionId, estudianteId, nota, usuario.nombre)
                }
            )
            if (sinNota.isNotEmpty()) calificacionDao.eliminarNotas(evaluacionId, sinNota)
            auditar(usuario, if (habiaNotas) "MODIFICAR" else "CREAR", detalle)
        }
        return ApiResult.Exito(Unit)
    }

    /** La firma digital es el usuario autenticado + la fecha y hora del momento (SUP-03). */
    override suspend fun registrarAnotacion(
        usuario: Usuario,
        estudianteId: String,
        tipo: TipoAnotacion,
        descripcion: String,
        detalle: String
    ): ApiResult<Unit> {
        db.withTransaction {
            anotacionDao.insertar(Anotacion(nuevoId(), estudianteId, tipo, descripcion, ahora(), usuario.nombre))
            auditar(usuario, "CREAR", detalle)
        }
        return ApiResult.Exito(Unit)
    }

    private suspend fun auditar(usuario: Usuario, accion: String, detalle: String) {
        auditoriaDao.insertar(RegistroAuditoria(nuevoId(), usuario.nombre, usuario.perfil, accion, detalle, ahora()))
    }

    private fun nuevoId(): String = UUID.randomUUID().toString()
}
