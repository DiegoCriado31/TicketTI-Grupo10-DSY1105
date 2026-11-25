package com.example.ticketti.ui.solicitud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ticketti.data.local.HistorialEntity

// Tarjeta de un movimiento del historial
@Composable
fun HistorialItem(registro: HistorialEntity, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(registro.accion, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "${registro.autor} · ${formatearFecha(registro.fecha)}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}