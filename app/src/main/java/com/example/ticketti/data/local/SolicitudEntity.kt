package com.example.ticketti.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "solicitudes")
data class SolicitudEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codigo: String = "",
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val servicio: String,
    val evidenciaUri: String? = null,
    val estado: String = "Recibida",
    val fechaCreacion: Long = System.currentTimeMillis(),
    val creadoPor: String = ""
)