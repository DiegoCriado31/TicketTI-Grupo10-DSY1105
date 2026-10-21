package com.example.ticketti.ui.solicitud

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent

private const val URL_SEGUIMIENTO = "https://ticketti.example.com/seguimiento/"

fun mensajeWhatsApp(codigo: String, titulo: String, categoria: String, estado: String): String =
    """
    *Solicitud TicketTI*
    Código: $codigo
    Título: $titulo
    Categoría: $categoria
    Estado: $estado
    Seguimiento: $URL_SEGUIMIENTO$codigo
    """.trimIndent()

fun compartirPorWhatsApp(context: Context, mensaje: String) {
    val envio = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, mensaje)
    }
    try {
        context.startActivity(Intent(envio).setPackage("com.whatsapp"))
    } catch (e: ActivityNotFoundException) {
        context.startActivity(Intent.createChooser(envio, "Compartir solicitud"))
    }
}
