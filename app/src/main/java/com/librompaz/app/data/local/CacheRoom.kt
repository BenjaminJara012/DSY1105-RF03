package com.librompaz.app.data.local

import androidx.room.withTransaction
import com.librompaz.app.data.MockData
import com.librompaz.app.model.CargaDocente
import com.librompaz.app.model.RegistroAsistencia
import com.librompaz.app.model.Usuario
import kotlinx.coroutines.flow.Flow

/** [CacheLocal] sobre Room (SQLite). */
class CacheRoom(private val db: AppDatabase) : CacheLocal {

    private val usuarioDao = db.usuarioDao()
    private val academicoDao = db.academicoDao()
    private val asistenciaDao = db.asistenciaDao()
    private val sesionDao = db.sesionDao()
    private val calificacionDao = db.calificacionDao()
    private val anotacionDao = db.anotacionDao()
    private val auditoriaDao = db.auditoriaDao()

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

    // RBAC (RF-01): la consulta filtra por docenteId en SQL.
    override fun cargasDe(docenteId: String): Flow<List<CargaDocente>> = academicoDao.cargasDe(docenteId)

    override suspend fun buscarUsuario(usuario: String): Usuario? = usuarioDao.buscarPorUsuario(usuario)

    override suspend fun guardarUsuario(usuario: Usuario) = usuarioDao.guardar(usuario)

    override suspend fun asistenciaDe(cursoId: String, fecha: String): List<RegistroAsistencia> =
        asistenciaDao.delCursoEnFecha(cursoId, fecha)

    override suspend fun reemplazar(datos: InstantaneaServidor) {
        db.withTransaction {
            // Se borra primero lo que depende de otras tablas (claves foráneas) y se inserta primero lo que es "padre".
            auditoriaDao.borrarCopiaLocal()
            anotacionDao.borrarCopiaLocal()
            calificacionDao.borrarNotas()
            calificacionDao.borrarEvaluaciones()
            sesionDao.borrarTodas()
            asistenciaDao.borrarTodas()
            academicoDao.borrarCargas()
            academicoDao.borrarObjetivos()
            academicoDao.borrarEstudiantes()
            academicoDao.borrarAsignaturas()
            academicoDao.borrarCursos()

            academicoDao.insertarCursos(datos.cursos)
            academicoDao.insertarAsignaturas(datos.asignaturas)
            academicoDao.insertarEstudiantes(datos.estudiantes)
            academicoDao.insertarObjetivos(datos.objetivos)
            academicoDao.insertarCargas(datos.cargas)
            asistenciaDao.guardar(datos.asistencias)
            sesionDao.insertarTodas(datos.sesiones)
            calificacionDao.insertarEvaluaciones(datos.evaluaciones)
            calificacionDao.guardarNotas(datos.calificaciones)
            // Las consultas ordenan por fecha y, en empate, por rowid DESC (el último insertado primero):
            // el servidor entrega lo más reciente primero, así que se inserta al revés.
            anotacionDao.insertarTodas(datos.anotaciones.asReversed())
            auditoriaDao.insertarTodos(datos.auditoria.asReversed())
        }
    }

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
}
