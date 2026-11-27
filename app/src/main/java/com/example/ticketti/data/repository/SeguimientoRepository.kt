package com.example.ticketti.data.repository

import com.example.ticketti.data.remote.ApiClient
import com.example.ticketti.data.remote.SeguimientoApi
import com.example.ticketti.data.remote.SolicitudSeguimiento
import kotlin.coroutines.cancellation.CancellationException

class SeguimientoRepository(
    private val api: SeguimientoApi = ApiClient.seguimientoApi
) {
    suspend fun obtenerSolicitudes(): Result<List<SolicitudSeguimiento>> =
        try {
            Result.success(api.obtenerSolicitudes())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}
