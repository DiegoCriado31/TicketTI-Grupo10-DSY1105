package com.example.ticketti.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comentarios")
data class ComentarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val solicitudId: Long,
    val autor: String,
    val texto: String,
    val evidenciaUri: String? = null,
    val fecha: Long = System.currentTimeMillis()
)