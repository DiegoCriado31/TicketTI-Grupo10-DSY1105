package com.example.ticketti.ui.solicitud

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun colorEstado(estado: String): Color = when (estado) {
    "Recibida" -> Color(0xFF1565C0)
    "En revisión" -> Color(0xFF6A1B9A)
    "Pendiente de información" -> Color(0xFFEF6C00)
    "Asignada" -> Color(0xFF00838F)
    "En proceso" -> Color(0xFFF9A825)
    "Resuelta" -> Color(0xFF2E7D32)
    "Cerrada" -> Color(0xFF455A64)
    "Cancelada" -> Color(0xFFC62828)
    else -> Color(0xFF757575)
}

@Composable
fun EstadoEtiqueta(estado: String, modifier: Modifier = Modifier) {
    val fondo by animateColorAsState(
        targetValue = colorEstado(estado),
        animationSpec = tween(durationMillis = 400),
        label = "colorEstado"
    )
    Surface(
        color = fondo,
        contentColor = Color.White,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Text(
            text = estado,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
