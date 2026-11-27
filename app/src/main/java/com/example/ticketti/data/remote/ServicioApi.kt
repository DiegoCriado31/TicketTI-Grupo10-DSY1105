package com.example.ticketti.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// Un servicio del catálogo externo (los nombres coinciden con el JSON)
data class ServicioCatalogo(
    val id: Int,
    val servicio: String,
    val categoria: String,
    val descripcion: String
)

// Endpoint: se agrega al final de la dirección base
interface ServicioApi {
    @GET("catalogo.json")
    suspend fun obtenerServicios(): List<ServicioCatalogo>
}

// Crea Retrofit una sola vez para toda la app
object ApiClient {
    private const val BASE_URL =
        "https://raw.githubusercontent.com/AbrahJosee/ticketti-api-grupo10/main/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val servicioApi: ServicioApi by lazy { retrofit.create(ServicioApi::class.java) }

    val seguimientoApi: SeguimientoApi by lazy { retrofit.create(SeguimientoApi::class.java) }
}