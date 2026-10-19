package com.example.ticketti.data.repository

import com.example.ticketti.data.local.SolicitudDao
import com.example.ticketti.data.local.SolicitudEntity
import kotlinx.coroutines.flow.Flow
import java.util.Locale

class SolicitudRepository(private val dao: SolicitudDao) {

    val solicitudes: Flow<List<SolicitudEntity>> = dao.observarTodas()

    suspend fun obtener(id: Long): SolicitudEntity? = dao.obtenerPorId(id)

    // Si id == 0 crea una nueva (y le asigna el código TI-0001, TI-0002...).
    // Si id != 0 actualiza la existente.
    suspend fun guardar(solicitud: SolicitudEntity): Long {
        return if (solicitud.id == 0L) {
            val nuevoId = dao.insertar(solicitud)
            val codigo = String.format(Locale.US, "TI-%04d", nuevoId)
            dao.actualizar(solicitud.copy(id = nuevoId, codigo = codigo))
            nuevoId
        } else {
            dao.actualizar(solicitud)
            solicitud.id
        }
    }
    suspend fun eliminar(solicitud: SolicitudEntity) {
        dao.eliminar(solicitud)
    }
}