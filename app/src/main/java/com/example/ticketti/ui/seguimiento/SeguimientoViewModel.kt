package com.example.ticketti.ui.seguimiento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ticketti.data.remote.SolicitudSeguimiento
import com.example.ticketti.data.repository.SeguimientoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SeguimientoUiState(
    val cargando: Boolean = true,
    val solicitudes: List<SolicitudSeguimiento> = emptyList(),
    val busqueda: String = "",
    val error: String? = null
) {
    val filtradas: List<SolicitudSeguimiento>
        get() = solicitudes.filter { it.codigo.contains(busqueda.trim(), ignoreCase = true) }
}

class SeguimientoViewModel(
    private val repo: SeguimientoRepository = SeguimientoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeguimientoUiState())
    val uiState: StateFlow<SeguimientoUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            repo.obtenerSolicitudes()
                .onSuccess { lista -> _uiState.update { it.copy(cargando = false, solicitudes = lista) } }
                .onFailure { _uiState.update { it.copy(cargando = false, error = "No se pudo conectar con la API") } }
        }
    }

    fun onBusquedaChange(valor: String) {
        _uiState.update { it.copy(busqueda = valor) }
    }
}
