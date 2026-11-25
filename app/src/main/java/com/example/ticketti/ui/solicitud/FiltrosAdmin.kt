package com.example.ticketti.ui.solicitud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ticketti.model.Credential

// Un desplegable de filtro con la opción "Todos" para quitarlo
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltroDesplegable(
    etiqueta: String,
    opciones: List<String>,
    seleccionado: String?,
    onSeleccion: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var abierto by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = abierto,
        onExpandedChange = { abierto = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = seleccionado ?: "Todos",
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = abierto) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = abierto,
            onDismissRequest = { abierto = false }
        ) {
            DropdownMenuItem(
                text = { Text("Todos") },
                onClick = {
                    onSeleccion(null)
                    abierto = false
                }
            )
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSeleccion(opcion)
                        abierto = false
                    }
                )
            }
        }
    }
}

// Barra con los cuatro filtros del admin
@Composable
fun FiltrosAdmin(viewModel: SolicitudViewModel) {
    val filtros by viewModel.filtros.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FiltroDesplegable(
                etiqueta = "Cliente",
                opciones = Credential.Clientes.map { it.username },
                seleccionado = filtros.cliente,
                onSeleccion = viewModel::filtrarPorCliente,
                modifier = Modifier.weight(1f)
            )
            FiltroDesplegable(
                etiqueta = "Estado",
                opciones = ESTADOS,
                seleccionado = filtros.estado,
                onSeleccion = viewModel::filtrarPorEstado,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FiltroDesplegable(
                etiqueta = "Prioridad",
                opciones = PRIORIDADES,
                seleccionado = filtros.prioridad,
                onSeleccion = viewModel::filtrarPorPrioridad,
                modifier = Modifier.weight(1f)
            )
            FiltroDesplegable(
                etiqueta = "Responsable",
                opciones = listOf(SIN_ASIGNAR) + Credential.Tecnicos.map { it.username },
                seleccionado = filtros.responsable,
                onSeleccion = viewModel::filtrarPorResponsable,
                modifier = Modifier.weight(1f)
            )
        }
        if (filtros.hayFiltros) {
            TextButton(onClick = viewModel::limpiarFiltros) {
                Text("Limpiar filtros")
            }
        }
    }
}