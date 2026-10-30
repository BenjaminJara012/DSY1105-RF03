package com.librompaz.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface AsistenciaDao {

    @Query("SELECT * FROM asistencias WHERE cursoId = :cursoId AND fecha = :fecha")
    suspend fun delCursoEnFecha(cursoId: String, fecha: String): List<AsistenciaEntity>

    @Upsert
    suspend fun guardar(registros: List<AsistenciaEntity>)

    @Query("SELECT * FROM asistencias")
    suspend fun todas(): List<AsistenciaEntity>
}