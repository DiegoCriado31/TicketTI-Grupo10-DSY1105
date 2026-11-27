package com.example.ticketti.data.remote

import retrofit2.http.GET

data class SolicitudSeguimiento(
    val codigo: String,
    val titulo: String,
    val categoria: String,
    val estado: String,
    val actualizacion: String
)

interface SeguimientoApi {
    @GET("solicitudes.json")
    suspend fun obtenerSolicitudes(): List<SolicitudSeguimiento>
}
