package com.example.ticketti.ui.solicitud

import com.example.ticketti.data.local.HistorialEntity
import com.example.ticketti.data.local.ComentarioEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ticketti.data.local.AppDatabase
import com.example.ticketti.data.local.SolicitudEntity
import com.example.ticketti.data.repository.SolicitudRepository
import com.example.ticketti.model.Credential
import com.example.ticketti.model.Rol
import com.example.ticketti.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Categorías en las que el servicio relacionado es obligatorio
val CATEGORIAS_CON_SERVICIO = setOf("Cambio en sitio", "Hosting / dominio")

// Estados que el técnico puede poner en una solicitud que tiene asignada
val ESTADOS_TECNICO = listOf("En revisión", "Resuelta")
// Prioridades que el admin puede definir (toda solicitud nace en "Baja")
val PRIORIDADES = listOf("Alta", "Media", "Baja")

// Todos los estados posibles, para el filtro del admin
val ESTADOS = listOf(
    "Recibida", "En revisión", "Pendiente de información", "Asignada",
    "En proceso", "Resuelta", "Cerrada", "Cancelada"
)

// Opción especial del filtro de responsable
const val SIN_ASIGNAR = "Sin asignar"

// Filtros activos del admin (null = sin filtrar por ese campo)
data class FiltrosSolicitud(
    val cliente: String? = null,
    val estado: String? = null,
    val prioridad: String? = null,
    val responsable: String? = null
) {
    val hayFiltros: Boolean
        get() = cliente != null || estado != null || prioridad != null || responsable != null
}

// Al asignar técnico, solo estos estados pasan automáticamente a "Asignada"
private val ESTADOS_ASIGNABLES = setOf("Recibida", "En revisión", "Pendiente de información")

class SolicitudViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = SolicitudRepository(
        AppDatabase.getDatabase(app).solicitudDao(),
        AppDatabase.getDatabase(app).comentarioDao(),
        AppDatabase.getDatabase(app).historialDao()
    )

    // Estado del formulario (la UI solo lo observa)
    private val _uiState = MutableStateFlow(SolicitudUiState())
    val uiState: StateFlow<SolicitudUiState> = _uiState.asStateFlow()

    // Usuario que inició sesión (null hasta que entra al menú)
    private val _usuario = MutableStateFlow<Credential?>(null)
    val usuarioActual: StateFlow<Credential?> = _usuario.asStateFlow()

    // Filtros que usa el admin en la lista
    private val _filtros = MutableStateFlow(FiltrosSolicitud())
    val filtros: StateFlow<FiltrosSolicitud> = _filtros.asStateFlow()

    // Lista de solicitudes: el cliente solo ve las suyas, el técnico solo las que
    // tiene asignadas y el admin ve todas (aplicando sus filtros)
    val solicitudes: StateFlow<List<SolicitudEntity>> =
        combine(repository.solicitudes, _usuario, _filtros) { lista, usuario, filtros ->
            if (usuario == null) emptyList<SolicitudEntity>()
            else when (usuario.rol) {
                Rol.CLIENTE -> lista.filter { it.creadoPor.equals(usuario.username, ignoreCase = true) }
                Rol.TECNICO -> lista.filter { it.tecnicoAsignado.equals(usuario.username, ignoreCase = true) }
                Rol.ADMIN -> lista.filter { s ->
                    val okCliente = filtros.cliente == null ||
                            s.creadoPor.equals(filtros.cliente, ignoreCase = true)
                    val okEstado = filtros.estado == null || s.estado == filtros.estado
                    val okPrioridad = filtros.prioridad == null || s.prioridad == filtros.prioridad
                    val okResponsable = when (filtros.responsable) {
                        null -> true
                        SIN_ASIGNAR -> s.tecnicoAsignado == null
                        else -> s.tecnicoAsignado.equals(filtros.responsable, ignoreCase = true)
                    }
                    okCliente && okEstado && okPrioridad && okResponsable
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Id de la solicitud que está abierta en el formulario (null si es nueva)
    private val _solicitudIdAbierta = MutableStateFlow<Long?>(null)

    // Comentarios de la solicitud abierta (se actualizan solos al agregar uno)
    @OptIn(ExperimentalCoroutinesApi::class)
    val comentarios: StateFlow<List<ComentarioEntity>> =
        _solicitudIdAbierta.flatMapLatest { id ->
            if (id == null) flowOf(emptyList<ComentarioEntity>()) else repository.comentarios(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Historial de la solicitud abierta (se actualiza solo)
    @OptIn(ExperimentalCoroutinesApi::class)
    val historial: StateFlow<List<HistorialEntity>> =
        _solicitudIdAbierta.flatMapLatest { id ->
            if (id == null) flowOf(emptyList<HistorialEntity>()) else repository.historial(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Guarda una línea en el historial con el usuario que hizo el cambio
    private suspend fun registrar(solicitudId: Long, accion: String) {
        repository.registrarHistorial(solicitudId, accion, _usuario.value?.username.orEmpty())
    }

    // Solicitud que se está editando (null si es una nueva)
    private var solicitudCargada: SolicitudEntity? = null

    // ---- Sesión ----

    fun iniciarSesion(username: String) {
        _usuario.value = AuthRepository().buscarUsuario(username)
    }

    // ---- Cambios de campos (limpian su error al escribir) ----

    fun onTituloChange(valor: String) {
        _uiState.update { it.copy(titulo = valor, tituloError = null) }
    }

    fun onDescripcionChange(valor: String) {
        _uiState.update { it.copy(descripcion = valor, descripcionError = null) }
    }

    fun onCategoriaChange(valor: String) {
        _uiState.update { it.copy(categoria = valor, categoriaError = null, servicioError = null) }
    }

    fun onServicioChange(valor: String) {
        _uiState.update { it.copy(servicio = valor, servicioError = null) }
    }

    fun onEvidenciaChange(uri: String?) {
        _uiState.update { it.copy(evidenciaUri = uri) }
    }

    // ---- Abrir formulario ----

    fun nuevaSolicitud() {
        solicitudCargada = null
        _solicitudIdAbierta.value = null
        _uiState.value = SolicitudUiState()
    }

    fun cargarSolicitud(id: Long) {
        // Limpia primero para no mostrar un instante los datos de la solicitud anterior
        solicitudCargada = null
        _solicitudIdAbierta.value = id
        _uiState.value = SolicitudUiState()
        viewModelScope.launch {
            val entity = repository.obtener(id) ?: return@launch
            solicitudCargada = entity
            _uiState.value = SolicitudUiState(
                codigo = entity.codigo,
                titulo = entity.titulo,
                descripcion = entity.descripcion,
                categoria = entity.categoria,
                servicio = entity.servicio,
                evidenciaUri = entity.evidenciaUri,
                estado = entity.estado,
                tecnicoAsignado = entity.tecnicoAsignado,
                prioridad = entity.prioridad,
                fechaCreacion = entity.fechaCreacion
            )
        }
    }

    fun setSolicitudParaEditar(entity: SolicitudEntity?) {
        if (entity == null) {
            nuevaSolicitud()
            return
        }
        solicitudCargada = entity
        _uiState.value = SolicitudUiState(
            codigo = entity.codigo,
            titulo = entity.titulo,
            descripcion = entity.descripcion,
            categoria = entity.categoria,
            servicio = entity.servicio,
            evidenciaUri = entity.evidenciaUri,
            estado = entity.estado,
            tecnicoAsignado = entity.tecnicoAsignado,
            prioridad = entity.prioridad,
            fechaCreacion = entity.fechaCreacion
        )
    }

    // ---- Guardar ----

    fun guardar() {
        // Evita guardar dos veces si el usuario toca el botón rápido
        if (_uiState.value.guardando) return
        val s = _uiState.value

        val tituloError = if (s.titulo.isBlank()) "El título es obligatorio" else null
        val descripcionError = if (s.descripcion.isBlank()) "La descripción es obligatoria" else null
        val categoriaError = if (s.categoria.isBlank()) "Selecciona una categoría" else null
        val servicioError = if (s.categoria in CATEGORIAS_CON_SERVICIO && s.servicio.isBlank())
            "Indica el servicio relacionado" else null

        if (tituloError != null || descripcionError != null || categoriaError != null || servicioError != null) {
            _uiState.update {
                it.copy(
                    tituloError = tituloError,
                    descripcionError = descripcionError,
                    categoriaError = categoriaError,
                    servicioError = servicioError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            val base = solicitudCargada
            val entity = SolicitudEntity(
                id = base?.id ?: 0L,
                codigo = base?.codigo ?: "",
                titulo = s.titulo.trim(),
                descripcion = s.descripcion.trim(),
                categoria = s.categoria,
                servicio = s.servicio.trim(),
                evidenciaUri = s.evidenciaUri,
                estado = base?.estado ?: "Recibida",
                tecnicoAsignado = base?.tecnicoAsignado,
                prioridad = base?.prioridad ?: "Baja",
                fechaCreacion = base?.fechaCreacion ?: System.currentTimeMillis(),
                creadoPor = base?.creadoPor ?: _usuario.value?.username.orEmpty()
            )
            repository.guardar(entity)
            _uiState.update { it.copy(guardando = false, guardadoExitosamente = true) }
        }
    }

    // ---- Asignación y cambio de estado ----

    // Solo el admin asigna. Si la solicitud aún no estaba en curso pasa a "Asignada".
    fun asignarTecnico(tecnico: String) {
        if (_usuario.value?.rol != Rol.ADMIN || tecnico.isBlank()) return
        val base = solicitudCargada ?: return
        val nuevoEstado = if (base.estado in ESTADOS_ASIGNABLES) "Asignada" else base.estado
        viewModelScope.launch {
            repository.asignarTecnico(base.id, tecnico, nuevoEstado)
            registrar(base.id, "Asignada al técnico $tecnico")
            solicitudCargada = base.copy(tecnicoAsignado = tecnico, estado = nuevoEstado)
            _uiState.update { it.copy(tecnicoAsignado = tecnico, estado = nuevoEstado) }
        }
    }

    // Solo el técnico asignado puede cambiar el estado (En revisión / Resuelta)
    fun cambiarEstado(nuevoEstado: String) {
        val usuario = _usuario.value ?: return
        if (usuario.rol != Rol.TECNICO || nuevoEstado !in ESTADOS_TECNICO) return
        val base = solicitudCargada ?: return
        if (!base.tecnicoAsignado.equals(usuario.username, ignoreCase = true)) return
        viewModelScope.launch {
            repository.cambiarEstado(base.id, nuevoEstado)
            registrar(base.id, "Estado: ${base.estado} → $nuevoEstado")
            solicitudCargada = base.copy(estado = nuevoEstado)
            _uiState.update { it.copy(estado = nuevoEstado) }
        }
    }

    // El cliente dueño de la solicitud confirma la resolución (Cerrada)
    // o pide reapertura (vuelve a En revisión). Solo si está en "Resuelta".
    fun responderResolucion(confirmar: Boolean) {
        val usuario = _usuario.value ?: return
        if (usuario.rol != Rol.CLIENTE) return
        val base = solicitudCargada ?: return
        if (base.estado != "Resuelta") return
        if (!base.creadoPor.equals(usuario.username, ignoreCase = true)) return
        val nuevoEstado = if (confirmar) "Cerrada" else "En revisión"
        viewModelScope.launch {
            repository.cambiarEstado(base.id, nuevoEstado)
            registrar(
                base.id,
                if (confirmar) "Resolución confirmada: Cerrada"
                else "Reapertura solicitada: En revisión"
            )
            solicitudCargada = base.copy(estado = nuevoEstado)
            _uiState.update { it.copy(estado = nuevoEstado) }
        }
    }

    // El cliente dueño puede reabrir una solicitud que ya está Cerrada
    fun solicitarReapertura() {
        val usuario = _usuario.value ?: return
        if (usuario.rol != Rol.CLIENTE) return
        val base = solicitudCargada ?: return
        if (base.estado != "Cerrada") return
        if (!base.creadoPor.equals(usuario.username, ignoreCase = true)) return
        viewModelScope.launch {
            repository.cambiarEstado(base.id, "En revisión")
            registrar(base.id, "Reapertura solicitada: En revisión")
            solicitudCargada = base.copy(estado = "En revisión")
            _uiState.update { it.copy(estado = "En revisión") }
        }
    }

    // Solo el admin cierra o cancela.
    // Cerrar: únicamente si está "Resuelta". Cancelar: cualquier estado no final.
    fun cerrarOCancelar(cerrar: Boolean) {
        if (_usuario.value?.rol != Rol.ADMIN) return
        val base = solicitudCargada ?: return
        if (base.estado == "Cerrada" || base.estado == "Cancelada") return
        if (cerrar && base.estado != "Resuelta") return
        val nuevoEstado = if (cerrar) "Cerrada" else "Cancelada"
        viewModelScope.launch {
            repository.cambiarEstado(base.id, nuevoEstado)
            registrar(base.id, "Estado: ${base.estado} → $nuevoEstado")
            solicitudCargada = base.copy(estado = nuevoEstado)
            _uiState.update { it.copy(estado = nuevoEstado) }
        }
    }

    // Solo el admin define la prioridad, después de revisar la solicitud y su evidencia
    fun cambiarPrioridad(prioridad: String) {
        if (_usuario.value?.rol != Rol.ADMIN || prioridad !in PRIORIDADES) return
        val base = solicitudCargada ?: return
        viewModelScope.launch {
            repository.cambiarPrioridad(base.id, prioridad)
            registrar(base.id, "Prioridad: ${base.prioridad} → $prioridad")
            solicitudCargada = base.copy(prioridad = prioridad)
            _uiState.update { it.copy(prioridad = prioridad) }
        }
    }

    // ---- Filtros del admin ----

    fun filtrarPorCliente(valor: String?) { _filtros.update { it.copy(cliente = valor) } }
    fun filtrarPorEstado(valor: String?) { _filtros.update { it.copy(estado = valor) } }
    fun filtrarPorPrioridad(valor: String?) { _filtros.update { it.copy(prioridad = valor) } }
    fun filtrarPorResponsable(valor: String?) { _filtros.update { it.copy(responsable = valor) } }
    fun limpiarFiltros() { _filtros.value = FiltrosSolicitud() }

    // ---- Comentarios del técnico ----

    fun onComentarioTextoChange(valor: String) {
        _uiState.update { it.copy(comentarioTexto = valor, comentarioError = null) }
    }

    fun onComentarioFotoChange(uri: String?) {
        _uiState.update { it.copy(comentarioFoto = uri) }
    }

    // Solo el técnico asignado a la solicitud puede comentar
    fun agregarComentario() {
        val usuario = _usuario.value ?: return
        if (usuario.rol != Rol.TECNICO) return
        val base = solicitudCargada ?: return
        if (!base.tecnicoAsignado.equals(usuario.username, ignoreCase = true)) return

        val s = _uiState.value
        if (s.comentarioTexto.isBlank()) {
            _uiState.update { it.copy(comentarioError = "Escribe un comentario") }
            return
        }
        viewModelScope.launch {
            repository.agregarComentario(
                ComentarioEntity(
                    solicitudId = base.id,
                    autor = usuario.username,
                    texto = s.comentarioTexto.trim(),
                    evidenciaUri = s.comentarioFoto
                )
            )
            _uiState.update { it.copy(comentarioTexto = "", comentarioFoto = null, comentarioError = null) }
        }
    }

    // ---- Eliminar ----

    fun eliminar() {
        // Solo el admin puede eliminar
        if (_usuario.value?.rol != Rol.ADMIN) return
        val base = solicitudCargada ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            repository.eliminar(base)
            solicitudCargada = null
            // Reutiliza la bandera para que la pantalla vuelva a la lista
            _uiState.update { it.copy(guardando = false, guardadoExitosamente = true) }
        }
    }

    // La UI llama esto después de navegar atrás, para no repetir la navegación
    fun consumirGuardado() {
        _uiState.update { it.copy(guardadoExitosamente = false) }
    }
}