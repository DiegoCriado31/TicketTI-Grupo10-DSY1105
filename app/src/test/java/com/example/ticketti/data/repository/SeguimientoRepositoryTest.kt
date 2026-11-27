package com.example.ticketti.data.repository

import com.example.ticketti.data.remote.SeguimientoApi
import com.example.ticketti.data.remote.SolicitudSeguimiento
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException

class SeguimientoRepositoryTest : StringSpec({

    "obtenerSolicitudes devuelve las solicitudes de la API" {
        val api = mockk<SeguimientoApi>()
        val solicitudes = listOf(
            SolicitudSeguimiento("TI-1001", "Sitio web no carga", "Error en sitio", "En proceso", "2026-11-20")
        )
        coEvery { api.obtenerSolicitudes() } returns solicitudes

        val resultado = SeguimientoRepository(api).obtenerSolicitudes()

        resultado.getOrNull() shouldBe solicitudes
    }

    "obtenerSolicitudes informa el error cuando la API falla" {
        val api = mockk<SeguimientoApi>()
        coEvery { api.obtenerSolicitudes() } throws IOException("sin internet")

        val resultado = SeguimientoRepository(api).obtenerSolicitudes()

        resultado.isFailure shouldBe true
    }
})
