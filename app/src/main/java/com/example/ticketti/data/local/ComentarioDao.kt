package com.example.ticketti.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ComentarioDao {

    @Query("SELECT * FROM comentarios WHERE solicitudId = :solicitudId ORDER BY fecha DESC")
    fun observarPorSolicitud(solicitudId: Long): Flow<List<ComentarioEntity>>

    @Insert
    suspend fun insertar(entity: ComentarioEntity): Long

    // Para no dejar comentarios huérfanos cuando el admin elimina una solicitud
    @Query("DELETE FROM comentarios WHERE solicitudId = :solicitudId")
    suspend fun eliminarPorSolicitud(solicitudId: Long)
}