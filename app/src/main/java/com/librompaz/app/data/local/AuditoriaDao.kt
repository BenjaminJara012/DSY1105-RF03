package com.librompaz.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditoriaDao {

    @Insert
    suspend fun insertar(registro: AuditoriaEntity)

    @Query("SELECT * FROM auditoria ORDER BY fechaHora DESC")
    fun todas(): Flow<List<AuditoriaEntity>>
}