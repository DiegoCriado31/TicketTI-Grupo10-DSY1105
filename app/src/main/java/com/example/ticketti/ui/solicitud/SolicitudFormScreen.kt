package com.example.ticketti.ui.solicitud

import com.example.ticketti.model.Credential
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.Alignment
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

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
    val esNueva = solicitudId == null
    val puedeEditarDatos = when (usuario?.rol) {
        Rol.CLIENTE -> esNueva || estado.estado == "Recibida"
        Rol.ADMIN -> true
        else -> false
    }
    var menuAbierto by remember { mutableStateOf(false) }
    var confirmarEliminar by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var uriPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    val tomarFoto =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
            if (exito) viewModel.onEvidenciaChange(uriPendiente)
            uriPendiente = null
        }
    val scope = rememberCoroutineScope()
    val elegirDeGaleria =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { origen ->
            if (origen != null) {
                scope.launch {
                    val copia = copiarEvidenciaDeGaleria(context, origen)
                    if (copia != null) {
                        viewModel.onEvidenciaChange(copia)
                    } else {
                        Toast.makeText(
                            context,
                            "La imagen no es válida o supera los 5 MB",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

    // Foto del comentario del técnico (independiente de la evidencia del cliente)
    var uriPendienteComentario by rememberSaveable { mutableStateOf<String?>(null) }
    val tomarFotoComentario =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
            if (exito) viewModel.onComentarioFotoChange(uriPendienteComentario)
            uriPendienteComentario = null
        }
    val elegirDeGaleriaComentario =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { origen ->
            if (origen != null) {
                scope.launch {
                    val copia = copiarEvidenciaDeGaleria(context, origen)
                    if (copia != null) {
                        viewModel.onComentarioFotoChange(copia)
                    } else {
                        Toast.makeText(
                            context,
                            "La imagen no es válida o supera los 5 MB",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    val comentarios by viewModel.comentarios.collectAsState()
    val historial by viewModel.historial.collectAsState()
    // Solo el técnico asignado a esta solicitud puede comentar
    val puedeComentar = usuario?.rol == Rol.TECNICO && !esNueva &&
            estado.tecnicoAsignado.equals(usuario?.username, ignoreCase = true)

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${estado.codigo} · Creada el ${formatearFecha(fecha)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    EstadoEtiqueta(estado.estado)
                }
            }

            // Técnico asignado (solo lectura; el admin lo gestiona con el desplegable)
            if (usuario?.rol != Rol.ADMIN) {
                estado.tecnicoAsignado?.let {
                    Text(
                        text = "Técnico asignado: $it",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Campo título
            OutlinedTextField(
                value = estado.titulo,
                onValueChange = viewModel::onTituloChange,
                readOnly = !puedeEditarDatos,
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
                readOnly = !puedeEditarDatos,
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
                onExpandedChange = { if (puedeEditarDatos) menuAbierto = it }
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
                readOnly = !puedeEditarDatos,
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
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        archivo
                    )
                    uriPendiente = uri.toString()
                    tomarFoto.launch(uri)
                },
                enabled = !estado.guardando && puedeEditarDatos,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (estado.evidenciaUri != null) "Cambiar foto" else "Tomar foto")
            }

            OutlinedButton(
                onClick = {
                    elegirDeGaleria.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                enabled = !estado.guardando && puedeEditarDatos,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Elegir de galería")
            }

            // Vista previa de la foto
            estado.evidenciaUri?.let { uriTexto ->
                val bitmap by produceState<Bitmap?>(initialValue = null, uriTexto) {
                    value = withContext(Dispatchers.IO) {
                        try {
                            val opciones = BitmapFactory.Options().apply { inSampleSize = 4 }
                            context.contentResolver.openInputStream(Uri.parse(uriTexto))
                                ?.use { flujo ->
                                    BitmapFactory.decodeStream(flujo, null, opciones)
                                }
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                AnimatedVisibility(
                    visible = bitmap != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    bitmap?.let { img ->
                        Image(
                            bitmap = img.asImageBitmap(),
                            contentDescription = "Evidencia de la solicitud",
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                    }
                }
            }

            // ---- Admin: asignar técnico ----
            if (usuario?.rol == Rol.ADMIN && !esNueva) {
                var menuTecnico by remember { mutableStateOf(false) }
                var tecnicoElegido by remember(estado.tecnicoAsignado) {
                    mutableStateOf(estado.tecnicoAsignado.orEmpty())
                }
                ExposedDropdownMenuBox(
                    expanded = menuTecnico,
                    onExpandedChange = { menuTecnico = it }
                ) {
                    OutlinedTextField(
                        value = tecnicoElegido,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Técnico asignado") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuTecnico)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = menuTecnico,
                        onDismissRequest = { menuTecnico = false }
                    ) {
                        Credential.Tecnicos.forEach { tecnico ->
                            DropdownMenuItem(
                                text = { Text(tecnico.username) },
                                onClick = {
                                    tecnicoElegido = tecnico.username
                                    menuTecnico = false
                                }
                            )
                        }
                    }
                }
                Button(
                    onClick = {
                        viewModel.asignarTecnico(tecnicoElegido)
                        Toast.makeText(
                            context,
                            "Técnico asignado: $tecnicoElegido",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    enabled = tecnicoElegido.isNotBlank() && tecnicoElegido != estado.tecnicoAsignado,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (estado.tecnicoAsignado == null) "Asignar técnico" else "Reasignar técnico")
                }
                Text("Prioridad", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PRIORIDADES.forEach { opcion ->
                        if (estado.prioridad == opcion) {
                            Button(onClick = {}, modifier = Modifier.weight(1f)) { Text(opcion) }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.cambiarPrioridad(opcion) },
                                modifier = Modifier.weight(1f)
                            ) { Text(opcion) }
                        }
                    }
                }
            }

            // ---- Admin: cerrar o cancelar la solicitud ----
            if (usuario?.rol == Rol.ADMIN && !esNueva) {
                var confirmarCancelar by remember { mutableStateOf(false) }
                val esFinal = estado.estado == "Cerrada" || estado.estado == "Cancelada"

                if (esFinal) {
                    Text(
                        text = "Esta solicitud está ${estado.estado.lowercase()}.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Button(
                        onClick = { viewModel.cerrarOCancelar(true) },
                        enabled = estado.estado == "Resuelta",
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar solicitud")
                    }
                    if (estado.estado != "Resuelta") {
                        Text(
                            text = "Solo se puede cerrar cuando está Resuelta.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    OutlinedButton(
                        onClick = { confirmarCancelar = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar solicitud", color = MaterialTheme.colorScheme.error)
                    }
                }

                if (confirmarCancelar) {
                    AlertDialog(
                        onDismissRequest = { confirmarCancelar = false },
                        title = { Text("Cancelar solicitud") },
                        text = { Text("¿Seguro que quieres cancelar esta solicitud?") },
                        confirmButton = {
                            TextButton(onClick = {
                                confirmarCancelar = false
                                viewModel.cerrarOCancelar(false)
                            }) { Text("Sí, cancelar") }
                        },
                        dismissButton = {
                            TextButton(onClick = { confirmarCancelar = false }) { Text("Volver") }
                        }
                    )
                }
            }

            // ---- Técnico: cambiar estado (En revisión / Resuelta) ----
            val estadoFinal = estado.estado == "Cerrada" || estado.estado == "Cancelada"
            if (usuario?.rol == Rol.TECNICO && !esNueva && !estadoFinal) {
                Text("Actualizar estado", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ESTADOS_TECNICO.forEach { opcion ->
                        val seleccionado = estado.estado == opcion
                        if (seleccionado) {
                            Button(onClick = {}, modifier = Modifier.weight(1f)) { Text(opcion) }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.cambiarEstado(opcion) },
                                modifier = Modifier.weight(1f)
                            ) { Text(opcion) }
                        }
                    }
                }
            }

            // ---- Cliente: confirmar resolución o pedir reapertura ----
            if (usuario?.rol == Rol.CLIENTE && !esNueva && estado.estado == "Resuelta") {
                Text("¿Quedó resuelta tu solicitud?", style = MaterialTheme.typography.titleMedium)
                Button(
                    onClick = { viewModel.responderResolucion(true) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirmar resolución")
                }
                OutlinedButton(
                    onClick = { viewModel.responderResolucion(false) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Solicitar reapertura")
                }
            }

            // ---- Cliente: reabrir una solicitud cerrada ----
            if (usuario?.rol == Rol.CLIENTE && !esNueva && estado.estado == "Cerrada") {
                Text("Esta solicitud está cerrada.", style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(
                    onClick = { viewModel.solicitarReapertura() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Solicitar reapertura")
                }
            }

            // ---- Cliente: compartir la referencia por WhatsApp (simulado) ----
            if (usuario?.rol == Rol.CLIENTE && !esNueva) {
                OutlinedButton(
                    onClick = {
                        compartirPorWhatsApp(
                            context,
                            mensajeWhatsApp(
                                codigo = estado.codigo,
                                titulo = estado.titulo,
                                categoria = estado.categoria,
                                estado = estado.estado
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compartir por WhatsApp")
                }
            }

            // ---- Comentarios (los ven todos; solo el técnico asignado escribe) ----
            if (!esNueva) {
                Text("Comentarios del técnico", style = MaterialTheme.typography.titleMedium)

                if (puedeComentar) {
                    OutlinedTextField(
                        value = estado.comentarioTexto,
                        onValueChange = viewModel::onComentarioTextoChange,
                        label = { Text("Nuevo comentario") },
                        minLines = 2,
                        isError = estado.comentarioError != null,
                        supportingText = {
                            estado.comentarioError?.let {
                                Text(text = it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedButton(
                        onClick = {
                            val carpeta = File(context.filesDir, "evidencias").apply { mkdirs() }
                            val archivo = File(carpeta, "avance_${System.currentTimeMillis()}.jpg")
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                archivo
                            )
                            uriPendienteComentario = uri.toString()
                            tomarFotoComentario.launch(uri)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (estado.comentarioFoto != null) "Cambiar foto del avance" else "Tomar foto del avance")
                    }

                    OutlinedButton(
                        onClick = {
                            elegirDeGaleriaComentario.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Elegir foto de galería")
                    }

                    estado.comentarioFoto?.let { uri ->
                        FotoEvidencia(
                            uriTexto = uri,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }

                    Button(
                        onClick = { viewModel.agregarComentario() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Agregar comentario")
                    }
                }

                if (comentarios.isEmpty()) {
                    Text("Aún no hay comentarios", style = MaterialTheme.typography.bodyMedium)
                } else {
                    comentarios.forEach { comentario ->
                        ComentarioItem(comentario = comentario)
                    }
                }
            }

            // ---- Historial de la solicitud (lo ven todos) ----
            if (!esNueva) {
                Text("Historial", style = MaterialTheme.typography.titleMedium)

                if (historial.isEmpty()) {
                    Text("Aún no hay movimientos", style = MaterialTheme.typography.bodyMedium)
                } else {
                    historial.forEach { registro ->
                        HistorialItem(registro = registro)
                    }
                }
            }

            // Botón guardar (solo si tiene permiso de editar)
            if (puedeEditarDatos) {
                Button(
                    onClick = { viewModel.guardar() },
                    enabled = !estado.guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (estado.guardando) "Guardando..." else "Guardar solicitud")
                }
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