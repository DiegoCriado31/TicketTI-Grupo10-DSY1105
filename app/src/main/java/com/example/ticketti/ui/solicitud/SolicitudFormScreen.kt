package com.example.ticketti.ui.solicitud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton

// Categorías del formulario (a confirmar con Abraham)
private val categorias = listOf(
    "Incidente web",
    "Cambio en sitio",
    "Soporte técnico",
    "Hosting / dominio"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudFormScreen(
    navController: NavController,
    viewModel: SolicitudViewModel,
    solicitudId: Long? = null   // null = solicitud nueva; con valor = editar
) {
    val estado by viewModel.uiState.collectAsState()
    var menuAbierto by remember { mutableStateOf(false) }
    var confirmarEliminar by remember { mutableStateOf(false) }
    // Al entrar: preparar formulario nuevo o cargar la solicitud a editar
    LaunchedEffect(solicitudId) {
        if (solicitudId != null) {
            viewModel.cargarSolicitud(solicitudId)
        } else {
            viewModel.nuevaSolicitud()
        }
    }

    // Cuando se guardó bien: reiniciar la bandera y volver a la lista
    LaunchedEffect(estado.guardadoExitosamente) {
        if (estado.guardadoExitosamente) {
            viewModel.consumirGuardado()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (solicitudId == null) "Nueva solicitud" else "Editar solicitud")
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Campo título
            OutlinedTextField(
                value = estado.titulo,
                onValueChange = viewModel::onTituloChange,
                label = { Text("Título") },
                isError = estado.tituloError != null,
                supportingText = {
                    estado.tituloError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Campo descripción
            OutlinedTextField(
                value = estado.descripcion,
                onValueChange = viewModel::onDescripcionChange,
                label = { Text("Descripción") },
                minLines = 3,
                isError = estado.descripcionError != null,
                supportingText = {
                    estado.descripcionError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Categoría (lista desplegable)
            ExposedDropdownMenuBox(
                expanded = menuAbierto,
                onExpandedChange = { menuAbierto = it }
            ) {
                OutlinedTextField(
                    value = estado.categoria,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuAbierto)
                    },
                    isError = estado.categoriaError != null,
                    supportingText = {
                        estado.categoriaError?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = menuAbierto,
                    onDismissRequest = { menuAbierto = false }
                ) {
                    categorias.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                viewModel.onCategoriaChange(opcion)
                                menuAbierto = false
                            }
                        )
                    }
                }
            }

            // Servicio relacionado (opcional)
            OutlinedTextField(
                value = estado.servicio,
                onValueChange = viewModel::onServicioChange,
                label = { Text("Servicio relacionado") },
                modifier = Modifier.fillMaxWidth()
            )

            // Evidencia (foto). La cámara se agrega después en pareja:
            // el botón, el permiso y el FileProvider llamarán a viewModel.onEvidenciaChange(uri)
            Text(
                text = if (estado.evidenciaUri != null) "Evidencia: foto adjunta"
                else "Evidencia: sin foto",
                style = MaterialTheme.typography.bodyMedium
            )

            // Botón guardar
            Button(
                onClick = { viewModel.guardar() },
                enabled = !estado.guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (estado.guardando) "Guardando..." else "Guardar solicitud")
            }
            // Eliminar (solo cuando se está editando una solicitud existente)
            if (solicitudId != null) {
                OutlinedButton(
                    onClick = { confirmarEliminar = true },
                    enabled = !estado.guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Eliminar solicitud", color = MaterialTheme.colorScheme.error)
                }
            }

            if (confirmarEliminar) {
                AlertDialog(
                    onDismissRequest = { confirmarEliminar = false },
                    title = { Text("Eliminar solicitud") },
                    text = { Text("¿Seguro que quieres eliminar esta solicitud? Esta acción no se puede deshacer.") },
                    confirmButton = {
                        TextButton(onClick = {
                            confirmarEliminar = false
                            viewModel.eliminar()
                        }) { Text("Eliminar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirmarEliminar = false }) { Text("Cancelar") }
                    }
                )
            }
        }
    }
}