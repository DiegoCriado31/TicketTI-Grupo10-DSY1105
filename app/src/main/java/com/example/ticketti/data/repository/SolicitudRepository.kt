package com.example.ticketti.data.repository

import com.example.ticketti.data.local.ComentarioDao
import com.example.ticketti.data.local.ComentarioEntity
import com.example.ticketti.data.local.HistorialDao
import com.example.ticketti.data.local.HistorialEntity
import com.example.ticketti.data.local.SolicitudDao
import com.example.ticketti.data.local.SolicitudEntity
import kotlinx.coroutines.flow.Flow
import java.util.Locale

class SolicitudRepository(
    private val dao: SolicitudDao,
    private val comentarioDao: ComentarioDao,
    private val historialDao: HistorialDao
) {

    val solicitudes: Flow<List<SolicitudEntity>> = dao.observarTodas()

    suspend fun obtener(id: Long): SolicitudEntity? = dao.obtenerPorId(id)

    // Si id == 0 crea una nueva (y le asigna el código TI-0001, TI-0002...).
    // Si id != 0 actualiza la existente.
    suspend fun guardar(solicitud: SolicitudEntity): Long {
        return if (solicitud.id == 0L) {
            val nuevoId = dao.insertar(solicitud)
            val codigo = String.format(Locale.US, "TI-%04d", nuevoId)
            dao.actualizar(solicitud.copy(id = nuevoId, codigo = codigo))
            registrarHistorial(nuevoId, "Solicitud creada", solicitud.creadoPor)
            nuevoId
        } else {
            // Al actualizar, el código, la fecha de creación y el autor NUNCA cambian:
            // se toman de lo que ya está guardado en la base.
            val original = dao.obtenerPorId(solicitud.id)
            dao.actualizar(
                solicitud.copy(
                    codigo = original?.codigo ?: solicitud.codigo,
                    fechaCreacion = original?.fechaCreacion ?: solicitud.fechaCreacion,
                    creadoPor = original?.creadoPor ?: solicitud.creadoPor
                )
            )
            solicitud.id
        }
    }

    suspend fun asignarTecnico(id: Long, tecnico: String, estado: String) {
        dao.actualizarAsignacion(id, tecnico, estado)
    }

    suspend fun cambiarEstado(id: Long, estado: String) {
        dao.actualizarEstado(id, estado)
    }

    fun comentarios(solicitudId: Long): Flow<List<ComentarioEntity>> =
        comentarioDao.observarPorSolicitud(solicitudId)

    suspend fun agregarComentario(comentario: ComentarioEntity) {
        comentarioDao.insertar(comentario)
    }

    suspend fun cambiarPrioridad(id: Long, prioridad: String) {
        dao.actualizarPrioridad(id, prioridad)
    }

    // ---- Historial ----

    fun historial(solicitudId: Long): Flow<List<HistorialEntity>> =
        historialDao.observarPorSolicitud(solicitudId)

    suspend fun registrarHistorial(solicitudId: Long, accion: String, autor: String) {
        historialDao.insertar(
            HistorialEntity(solicitudId = solicitudId, accion = accion, autor = autor)
        )
    }

    suspend fun eliminar(solicitud: SolicitudEntity) {
        comentarioDao.eliminarPorSolicitud(solicitud.id)
        historialDao.eliminarPorSolicitud(solicitud.id)
        dao.eliminar(solicitud)
    }
}