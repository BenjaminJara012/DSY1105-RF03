package com.librompaz.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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

/**
 * DATABASE: une todas las entidades (tablas) con sus DAO y entrega UNA sola instancia de la base
 * de datos para toda la app (singleton). El archivo SQLite se llama "libro_mpaz.db" y queda en
 * el almacenamiento interno de la app: los datos sobreviven al cierre de la app.
 *
 * Si se cambia una entidad (por ejemplo, se agrega una columna), hay que subir "version".
 */
@Database(
    entities = [
        Usuario::class,
        Curso::class,
        Asignatura::class,
        Estudiante::class,
        CargaDocente::class,
        RegistroAsistencia::class,
        ObjetivoAprendizaje::class,
        SesionClase::class,
        Evaluacion::class,
        Calificacion::class,
        Anotacion::class,
        RegistroAuditoria::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun academicoDao(): AcademicoDao
    abstract fun asistenciaDao(): AsistenciaDao
    abstract fun sesionDao(): SesionDao
    abstract fun calificacionDao(): CalificacionDao
    abstract fun anotacionDao(): AnotacionDao
    abstract fun auditoriaDao(): AuditoriaDao

    companion object {
        /** Copia local de lo que sirve la API (modo servidor). Es el archivo que ya usaba la app. */
        const val NOMBRE_SERVIDOR = "libro_mpaz.db"

        /**
         * Base propia del modo local: independiente de la copia del servidor, para que lo que se
         * crea sin servidor no se mezcle con lo que el servidor entrega (ni se pierda al sincronizar).
         */
        const val NOMBRE_LOCAL = "libro_mpaz_local.db"

        private val instancias = HashMap<String, AppDatabase>()

        fun getInstance(context: Context, nombre: String = NOMBRE_SERVIDOR): AppDatabase =
            synchronized(instancias) {
                instancias.getOrPut(nombre) {
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        nombre
                    )
                        // Prototipo con datos ficticios: si cambia el esquema sin migración, se recrea
                        // la base y se vuelve a poblar con los datos de ejemplo.
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
    }
}
