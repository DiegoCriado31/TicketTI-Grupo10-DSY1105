package com.example.ticketti.ui.solicitud

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ticketti.data.local.ComentarioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Carga y muestra una foto a partir de su uri en texto
@Composable
fun FotoEvidencia(uriTexto: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, uriTexto) {
        value = withContext(Dispatchers.IO) {
            try {
                val opciones = BitmapFactory.Options().apply { inSampleSize = 4 }
                context.contentResolver.openInputStream(Uri.parse(uriTexto))?.use { flujo ->
                    BitmapFactory.decodeStream(flujo, null, opciones)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    bitmap?.let { img ->
        Image(
            bitmap = img.asImageBitmap(),
            contentDescription = "Foto de evidencia",
            modifier = modifier
        )
    }
}

// Tarjeta de un comentario del técnico
@Composable
fun ComentarioItem(comentario: ComentarioEntity, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = comentario.autor,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formatearFecha(comentario.fecha),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(text = comentario.texto, style = MaterialTheme.typography.bodyMedium)
            comentario.evidenciaUri?.let { uri ->
                FotoEvidencia(
                    uriTexto = uri,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }
}