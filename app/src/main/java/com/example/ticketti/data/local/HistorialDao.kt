package com.example.ticketti.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistorialDao {

    @Insert
    suspend fun insertar(registro: HistorialEntity)

    @Query("SELECT * FROM historial WHERE solicitudId = :solicitudId ORDER BY fecha DESC")
    fun observarPorSolicitud(solicitudId: Long): Flow<List<HistorialEntity>>

    @Query("DELETE FROM historial WHERE solicitudId = :solicitudId")
    suspend fun eliminarPorSolicitud(solicitudId: Long)
}