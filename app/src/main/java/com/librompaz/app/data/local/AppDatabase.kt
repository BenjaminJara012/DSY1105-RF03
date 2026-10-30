package com.librompaz.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction

@Database(
    entities = [AsistenciaEntity::class, AuditoriaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun asistenciaDao(): AsistenciaDao
    abstract fun auditoriaDao(): AuditoriaDao

    suspend fun guardarConAuditoria(
        registros: List<AsistenciaEntity>,
        auditoria: AuditoriaEntity,
        cursoId: String,
        fecha: String
    ) = withTransaction {
        asistenciaDao().eliminarDelCursoEnFecha(cursoId, fecha)
        asistenciaDao().guardar(registros)
        auditoriaDao().insertar(auditoria)
    }

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "libro_mpaz.db"
                ).build().also { instancia = it }
            }
    }
}