package com.example.ticketti.ui.catalogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ticketti.data.remote.ServicioCatalogo
import com.example.ticketti.data.repository.CatalogoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CatalogoUiState(
    val cargando: Boolean = true,
    val servicios: List<ServicioCatalogo> = emptyList(),
    val desdeApi: Boolean = true
)

class CatalogoViewModel(
    private val repo: CatalogoRepository = CatalogoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogoUiState())
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)
            val resultado = repo.obtenerCatalogo()
            _uiState.value = CatalogoUiState(
                cargando = false,
                servicios = resultado.servicios,
                desdeApi = resultado.desdeApi
            )
        }
    }
}