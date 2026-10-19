package com.example.ticketti.ui.solicitud

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

class SolicitudViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = SolicitudRepository(
        AppDatabase.getDatabase(app).solicitudDao()
    )

    // Estado del formulario (la UI solo lo observa)
    private val _uiState = MutableStateFlow(SolicitudUiState())
    val uiState: StateFlow<SolicitudUiState> = _uiState.asStateFlow()

    // Usuario que inició sesión (null hasta que entra al menú)
    private val _usuario = MutableStateFlow<Credential?>(null)
    val usuarioActual: StateFlow<Credential?> = _usuario.asStateFlow()

    // Lista de solicitudes: el cliente solo ve las suyas; admin y técnico ven todas
    val solicitudes: StateFlow<List<SolicitudEntity>> =
        combine(repository.solicitudes, _usuario) { lista, usuario ->
            if (usuario == null) emptyList<SolicitudEntity>()
            else if (usuario.rol == Rol.CLIENTE)
                lista.filter { it.creadoPor.equals(usuario.username, ignoreCase = true) }
            else lista
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
        _uiState.value = SolicitudUiState()
    }

    fun cargarSolicitud(id: Long) {
        viewModelScope.launch {
            val entity = repository.obtener(id) ?: return@launch
            solicitudCargada = entity
            _uiState.value = SolicitudUiState(
                titulo = entity.titulo,
                descripcion = entity.descripcion,
                categoria = entity.categoria,
                servicio = entity.servicio,
                evidenciaUri = entity.evidenciaUri,
                estado = entity.estado
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
            titulo = entity.titulo,
            descripcion = entity.descripcion,
            categoria = entity.categoria,
            servicio = entity.servicio,
            evidenciaUri = entity.evidenciaUri,
            estado = entity.estado
        )
    }

    // ---- Guardar ----

    fun guardar() {
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
                fechaCreacion = base?.fechaCreacion ?: System.currentTimeMillis(),
                creadoPor = base?.creadoPor ?: _usuario.value?.username.orEmpty()
            )
            repository.guardar(entity)
            _uiState.update { it.copy(guardando = false, guardadoExitosamente = true) }
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