package com.example.ticketti.ui.solicitud

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.ticketti.model.Rol
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    val usuario by viewModel.usuarioActual.collectAsState()
    val puedeEliminar = usuario?.rol == Rol.ADMIN && solicitudId != null
    var menuAbierto by remember { mutableStateOf(false) }
    var confirmarEliminar by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var uriPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    val tomarFoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        if (exito) viewModel.onEvidenciaChange(uriPendiente)
        uriPendiente = null
    }
    // rememberSaveable sobrevive a rotar o volver de la cámara, pero se pierde
    // al salir del formulario. Así solo se inicializa UNA vez por visita.
    var inicializado by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(solicitudId) {
        if (!inicializado) {
            if (solicitudId != null) {
                viewModel.cargarSolicitud(solicitudId)
            } else {
                viewModel.nuevaSolicitud()
            }
            inicializado = true
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

            // Fecha y hora de creación (solo al editar; nunca cambia)
            estado.fechaCreacion?.let { fecha ->
                Text(
                    text = "Creada el ${formatearFecha(fecha)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Campo título
            OutlinedTextField(
                value = estado.titulo,
                onValueChange = viewModel::onTituloChange,
                label = { Text("Título") },
                leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
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
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
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
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
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

            // Servicio relacionado (obligatorio solo en algunas categorías)
            val servicioObligatorio = estado.categoria in CATEGORIAS_CON_SERVICIO
            OutlinedTextField(
                value = estado.servicio,
                onValueChange = viewModel::onServicioChange,
                label = {
                    Text(
                        if (servicioObligatorio) "Servicio relacionado (obligatorio)"
                        else "Servicio relacionado (opcional)"
                    )
                },
                leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) },
                isError = estado.servicioError != null,
                supportingText = {
                    estado.servicioError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Evidencia (foto) con la cámara del sistema
            OutlinedButton(
                onClick = {
                    val carpeta = File(context.filesDir, "evidencias").apply { mkdirs() }
                    val archivo = File(carpeta, "evidencia_${System.currentTimeMillis()}.jpg")
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
                    uriPendiente = uri.toString()
                    tomarFoto.launch(uri)
                },
                enabled = !estado.guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (estado.evidenciaUri != null) "Cambiar foto" else "Tomar foto")
            }

            // Vista previa de la foto
            estado.evidenciaUri?.let { uriTexto ->
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
                        contentDescription = "Evidencia de la solicitud",
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                }
            }

            // Botón guardar
            Button(
                onClick = { viewModel.guardar() },
                enabled = !estado.guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (estado.guardando) "Guardando..." else "Guardar solicitud")
            }

            // Eliminar (solo cuando se está editando una solicitud existente)
            if (puedeEliminar) {
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