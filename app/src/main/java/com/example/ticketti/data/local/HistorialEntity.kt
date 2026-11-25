package com.example.ticketti.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "historial")
data class HistorialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val solicitudId: Long,
    val accion: String,   // Ej: "Solicitud creada", "Estado: Recibida → En revisión"
    val autor: String,
    val fecha: Long = System.currentTimeMillis()
)