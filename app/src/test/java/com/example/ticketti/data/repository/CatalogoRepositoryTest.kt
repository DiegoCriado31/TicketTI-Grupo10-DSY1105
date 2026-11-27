package com.example.ticketti.data.repository

import com.example.ticketti.data.remote.ServicioApi
import com.example.ticketti.data.remote.ServicioCatalogo
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException

class CatalogoRepositoryTest : StringSpec({

    "obtenerCatalogo devuelve los servicios de la API" {
        val api = mockk<ServicioApi>()
        val servicios = listOf(
            ServicioCatalogo(10, "Soporte remoto", "Soporte", "Asistencia a distancia"),
            ServicioCatalogo(11, "Respaldo de datos", "Infraestructura", "Copias de seguridad")
        )
        coEvery { api.obtenerServicios() } returns servicios

        val resultado = CatalogoRepository(api).obtenerCatalogo()

        resultado.servicios shouldBe servicios
        resultado.desdeApi shouldBe true
        coVerify(exactly = 1) { api.obtenerServicios() }
    }

    "obtenerCatalogo usa el respaldo cuando la API falla" {
        val api = mockk<ServicioApi>()
        coEvery { api.obtenerServicios() } throws IOException("sin internet")

        val resultado = CatalogoRepository(api).obtenerCatalogo()

        resultado.servicios.size shouldBe 3
        resultado.desdeApi shouldBe false
    }

    "obtenerCatalogo usa el respaldo cuando la API responde vacia" {
        val api = mockk<ServicioApi>()
        coEvery { api.obtenerServicios() } returns emptyList()

        val resultado = CatalogoRepository(api).obtenerCatalogo()

        resultado.servicios.size shouldBe 3
        resultado.desdeApi shouldBe false
    }
})
