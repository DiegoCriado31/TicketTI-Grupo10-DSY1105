package com.example.ticketti.ui.solicitud

data class SolicitudUiState(
    val codigo: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val servicio: String = "",
    val evidenciaUri: String? = null,
    val estado: String = "Recibida",
    val fechaCreacion: Long? = null,
    val tituloError: String? = null,
    val descripcionError: String? = null,
    val categoriaError: String? = null,
    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false,
    val servicioError: String? = null
)