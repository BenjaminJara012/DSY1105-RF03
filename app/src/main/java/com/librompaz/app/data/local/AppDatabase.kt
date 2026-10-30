package com.librompaz.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.librompaz.app.data.MockData

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
        auditoria: AuditoriaEntity
    ) = withTransaction {
        asistenciaDao().guardar(registros)
        auditoriaDao().insertar(auditoria)
    }

    companion object {
        private val semilla = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                MockData.asistencias.forEach { r ->
                    db.execSQL(
                        "INSERT INTO asistencias (id, estudianteId, cursoId, fecha, estado, registradoPor, fechaHoraRegistro) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        arrayOf<Any?>(r.id, r.estudianteId, r.cursoId, r.fecha, r.estado.name, r.registradoPor, r.fechaHoraRegistro)
                    )
                }
                MockData.auditoria.forEach { a ->
                    db.execSQL(
                        "INSERT INTO auditoria (id, usuario, perfil, accion, detalle, fechaHora) VALUES (?, ?, ?, ?, ?, ?)",
                        arrayOf<Any?>(a.id, a.usuario, a.perfil.name, a.accion, a.detalle, a.fechaHora)
                    )
                }
            }
        }
        @Volatile
        private var instancia: AppDatabase? = null

        fun construir(context: Context, nombre: String = "libro_mpaz.db"): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, nombre)
                .addCallback(semilla)
                .build()

        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: construir(context).also { instancia = it }
            }
    }
}