package com.example.ticketti.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import androidx.room.Delete

@Dao
interface SolicitudDao {

    @Query("SELECT * FROM solicitudes ORDER BY id DESC")
    fun observarTodas(): Flow<List<SolicitudEntity>>

    @Query("SELECT * FROM solicitudes WHERE id = :id")
    suspend fun obtenerPorId(id: Long): SolicitudEntity?

    @Insert
    suspend fun insertar(entity: SolicitudEntity): Long

    @Update
    suspend fun actualizar(entity: SolicitudEntity)
    @Delete
    suspend fun eliminar(entity: SolicitudEntity)
}

