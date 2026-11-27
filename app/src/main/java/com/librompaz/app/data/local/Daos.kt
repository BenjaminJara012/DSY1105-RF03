package com.librompaz.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
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

// =====================================================================================
// DAOs (Data Access Object): las consultas a cada tabla. Room escribe la implementación al
// compilar (por eso se usa KSP).
//  - Las lecturas que las pantallas OBSERVAN devuelven Flow (no son suspend): Room emite una
//    lista nueva cada vez que la tabla cambia y la pantalla se actualiza sola.
//  - Las escrituras y las lecturas puntuales son suspend: tocan el disco y Room las ejecuta
//    fuera del hilo principal.
// =====================================================================================

/** Módulo "Autenticación y alcance por rol". */
@Dao
interface UsuarioDao {

    // Login: busca por correo sin distinguir mayúsculas. La clave se compara en el ViewModel.
    @Query("SELECT * FROM usuarios WHERE usuario = :usuario COLLATE NOCASE LIMIT 1")
    suspend fun buscarPorUsuario(usuario: String): Usuario?

    // Para saber si la base está vacía y hay que poblarla con los datos ficticios.
    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int

    @Insert
    suspend fun insertarTodos(usuarios: List<Usuario>)

    // Tras un ingreso con el servidor: guarda al usuario (y el hash de su clave) para poder
    // ingresar sin conexión. Upsert (no REPLACE): no borra la fila, así no rompe las cargas que la referencian.
    @Upsert
    suspend fun guardar(usuario: Usuario)
}

/** Datos de referencia: cursos, asignaturas, estudiantes, OA y carga docente. */
@Dao
interface AcademicoDao {

    @Query("SELECT * FROM cursos ORDER BY nombre")
    fun cursos(): Flow<List<Curso>>

    @Query("SELECT * FROM asignaturas ORDER BY nombre")
    fun asignaturas(): Flow<List<Asignatura>>

    @Query("SELECT * FROM estudiantes ORDER BY nombreFicticio")
    fun estudiantes(): Flow<List<Estudiante>>

    @Query("SELECT * FROM objetivos ORDER BY asignaturaId, codigo")
    fun objetivos(): Flow<List<ObjetivoAprendizaje>>

    @Query("SELECT * FROM cargas_docentes")
    fun cargas(): Flow<List<CargaDocente>>

    // RBAC (RF-01): SOLO las cargas del docente indicado.
    @Query("SELECT * FROM cargas_docentes WHERE docenteId = :docenteId")
    fun cargasDe(docenteId: String): Flow<List<CargaDocente>>

    @Insert
    suspend fun insertarCursos(cursos: List<Curso>)

    @Insert
    suspend fun insertarAsignaturas(asignaturas: List<Asignatura>)

    @Insert
    suspend fun insertarEstudiantes(estudiantes: List<Estudiante>)

    @Insert
    suspend fun insertarObjetivos(objetivos: List<ObjetivoAprendizaje>)

    @Insert
    suspend fun insertarCargas(cargas: List<CargaDocente>)

    // Sincronización con el servidor: se vacía la copia local y se vuelve a llenar en una transacción.
    @Query("DELETE FROM cargas_docentes")
    suspend fun borrarCargas()

    @Query("DELETE FROM objetivos")
    suspend fun borrarObjetivos()

    @Query("DELETE FROM estudiantes")
    suspend fun borrarEstudiantes()

    @Query("DELETE FROM asignaturas")
    suspend fun borrarAsignaturas()

    @Query("DELETE FROM cursos")
    suspend fun borrarCursos()
}

/** Módulo "Asistencia". */
@Dao
interface AsistenciaDao {

    @Query("SELECT * FROM asistencias ORDER BY fecha DESC")
    fun todas(): Flow<List<RegistroAsistencia>>

    // Lo ya registrado de un curso en una fecha (para mostrarlo y poder corregirlo).
    @Query("SELECT * FROM asistencias WHERE cursoId = :cursoId AND fecha = :fecha")
    suspend fun delCursoEnFecha(cursoId: String, fecha: String): List<RegistroAsistencia>

    // REPLACE (RN-05): el id es "<estudiante>_<fecha>", así que volver a guardar la fecha sobrescribe.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(registros: List<RegistroAsistencia>)

    @Query("DELETE FROM asistencias")
    suspend fun borrarTodas()
}

/** Módulo "Cobertura curricular". */
@Dao
interface SesionDao {

    @Query("SELECT * FROM sesiones ORDER BY fecha DESC, bloque")
    fun todas(): Flow<List<SesionClase>>

    @Insert
    suspend fun insertar(sesion: SesionClase)

    @Insert
    suspend fun insertarTodas(sesiones: List<SesionClase>)

    @Query("DELETE FROM sesiones")
    suspend fun borrarTodas()
}

/** Módulo "Calificaciones". */
@Dao
interface CalificacionDao {

    @Query("SELECT * FROM evaluaciones ORDER BY fecha DESC, nombreInstrumento")
    fun evaluaciones(): Flow<List<Evaluacion>>

    @Query("SELECT * FROM calificaciones")
    fun calificaciones(): Flow<List<Calificacion>>

    @Query("SELECT * FROM calificaciones WHERE evaluacionId = :evaluacionId")
    suspend fun notasDe(evaluacionId: String): List<Calificacion>

    @Insert
    suspend fun insertarEvaluacion(evaluacion: Evaluacion)

    @Insert
    suspend fun insertarEvaluaciones(evaluaciones: List<Evaluacion>)

    // Al borrar la evaluación, sus notas se borran solas (ForeignKey.CASCADE en Calificacion).
    @Delete
    suspend fun eliminarEvaluacion(evaluacion: Evaluacion)

    // REPLACE (RN-08): el id es "<evaluacion>_<estudiante>", así que corregir una nota la sobrescribe.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarNotas(notas: List<Calificacion>)

    // Notas que el docente dejó en blanco al corregir: se eliminan.
    @Query("DELETE FROM calificaciones WHERE evaluacionId = :evaluacionId AND estudianteId IN (:estudianteIds)")
    suspend fun eliminarNotas(evaluacionId: String, estudianteIds: List<String>)

    @Query("DELETE FROM calificaciones")
    suspend fun borrarNotas()

    @Query("DELETE FROM evaluaciones")
    suspend fun borrarEvaluaciones()
}

/** Módulo "Hoja de vida" (RN-06): solo se agregan anotaciones; no hay @Update ni @Delete. */
@Dao
interface AnotacionDao {

    @Query("SELECT * FROM anotaciones ORDER BY fechaHora DESC, rowid DESC")
    fun todas(): Flow<List<Anotacion>>

    @Insert
    suspend fun insertar(anotacion: Anotacion)

    @Insert
    suspend fun insertarTodas(anotaciones: List<Anotacion>)

    // Solo para la sincronización: la copia local se reemplaza por lo que entrega el servidor.
    // La app no ofrece ninguna operación para editar ni borrar una anotación (RN-06).
    @Query("DELETE FROM anotaciones")
    suspend fun borrarCopiaLocal()
}

/**
 * Auditoría (RN-02): INALTERABLE. A propósito este DAO no tiene @Update ni @Delete, así que
 * desde la app no hay forma de modificar ni borrar un registro de auditoría.
 */
@Dao
interface AuditoriaDao {

    @Query("SELECT * FROM auditoria ORDER BY fechaHora DESC, rowid DESC")
    fun todos(): Flow<List<RegistroAuditoria>>

    @Insert
    suspend fun insertar(registro: RegistroAuditoria)

    @Insert
    suspend fun insertarTodos(registros: List<RegistroAuditoria>)

    // Solo para la sincronización (copia local de lo que guarda el servidor); ninguna pantalla lo usa.
    @Query("DELETE FROM auditoria")
    suspend fun borrarCopiaLocal()
}
