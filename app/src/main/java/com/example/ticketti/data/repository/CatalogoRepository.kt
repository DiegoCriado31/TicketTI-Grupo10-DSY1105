package com.example.ticketti.data.repository

import com.example.ticketti.data.remote.ApiClient
import com.example.ticketti.data.remote.ServicioApi
import com.example.ticketti.data.remote.ServicioCatalogo
import kotlin.coroutines.cancellation.CancellationException

// Resultado: la lista y si vino de la API (true) o del respaldo local (false)
data class CatalogoResultado(
    val servicios: List<ServicioCatalogo>,
    val desdeApi: Boolean
)

class CatalogoRepository(
    private val api: ServicioApi = ApiClient.servicioApi
) {

    // Mismos datos del catálogo, por si no hay internet o la API falla
    private val respaldo = listOf(
        ServicioCatalogo(1, "Mantenimiento web", "Web", "Actualización de sitios"),
        ServicioCatalogo(2, "Soporte de correo", "Soporte", "Configuración de correo"),
        ServicioCatalogo(3, "Hosting", "Infraestructura", "Alojamiento web")
    )

    suspend fun obtenerCatalogo(): CatalogoResultado {
        return try {
            val servicios = api.obtenerServicios()
            if (servicios.isEmpty()) CatalogoResultado(respaldo, false)
            else CatalogoResultado(servicios, true)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            CatalogoResultado(respaldo, false)
        }
    }
}