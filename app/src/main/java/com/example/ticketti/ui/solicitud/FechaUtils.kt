package com.example.ticketti.ui.solicitud

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Convierte los milisegundos guardados en Room a texto, ej: "19/10/2026 14:35"
fun formatearFecha(millis: Long): String {
    val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-CL"))
    return formato.format(Date(millis))
}