package com.librompaz.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auditoria")
data class AuditoriaEntity(
    @PrimaryKey val id: String,
    val usuario: String,
    val perfil: String,
    val accion: String,
    val detalle: String,
    val fechaHora: String
)