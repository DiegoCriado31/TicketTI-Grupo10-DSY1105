package com.example.ticketti.ui.solicitud

import androidx.compose.ui.graphics.Color
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class SolicitudUtilidadesTest : StringSpec({

    "mensajeWhatsApp incluye los datos de la solicitud" {
        val mensaje = mensajeWhatsApp("TI-0001", "Error en el sitio", "Soporte", "Recibida")

        mensaje shouldContain "Código: TI-0001"
        mensaje shouldContain "Título: Error en el sitio"
        mensaje shouldContain "Categoría: Soporte"
        mensaje shouldContain "Estado: Recibida"
        mensaje shouldContain "seguimiento/TI-0001"
    }

    "colorEstado asigna un color a cada estado" {
        colorEstado("Recibida") shouldBe Color(0xFF1565C0)
        colorEstado("Resuelta") shouldBe Color(0xFF2E7D32)
        colorEstado("Cancelada") shouldBe Color(0xFFC62828)
    }

    "colorEstado usa gris para un estado desconocido" {
        colorEstado("Otro") shouldBe Color(0xFF757575)
    }
})
