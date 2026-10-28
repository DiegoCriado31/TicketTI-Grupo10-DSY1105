package com.example.ticketti.ui.solicitud

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

const val TAMANO_MAXIMO_EVIDENCIA = 5L * 1024 * 1024

suspend fun copiarEvidenciaDeGaleria(context: Context, origen: Uri): String? =
    withContext(Dispatchers.IO) {
        val carpeta = File(context.filesDir, "evidencias").apply { mkdirs() }
        val archivo = File(carpeta, "evidencia_${System.currentTimeMillis()}.jpg")
        try {
            context.contentResolver.openInputStream(origen)?.use { entrada ->
                archivo.outputStream().use { salida -> entrada.copyTo(salida) }
            }
            if (archivo.length() == 0L || archivo.length() > TAMANO_MAXIMO_EVIDENCIA) {
                archivo.delete()
                null
            } else {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo).toString()
            }
        } catch (e: Exception) {
            archivo.delete()
            null
        }
    }
