package com.example.ticketti.ui.catalogo

import com.example.ticketti.data.remote.ServicioApi
import com.example.ticketti.data.remote.ServicioCatalogo
import com.example.ticketti.data.repository.CatalogoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogoViewModelTest {

    // viewModelScope usa el hilo principal de Android; en la prueba lo reemplazamos
    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    @Test
    fun `si la API falla el ViewModel muestra el respaldo y deja de cargar`() {
        // API falsa que simula que no hay internet
        val apiSinInternet = object : ServicioApi {
            override suspend fun obtenerServicios(): List<ServicioCatalogo> {
                throw IOException("sin internet")
            }
        }

        // Al crearse, el ViewModel pide el catálogo (init { cargar() })
        val viewModel = CatalogoViewModel(CatalogoRepository(apiSinInternet))
        val estado = viewModel.uiState.value

        assertFalse(estado.cargando)                       // ya terminó de cargar
        assertFalse(estado.desdeApi)                       // los datos no vienen de la API
        assertEquals(3, estado.servicios.size)             // muestra el respaldo de 3 servicios
        assertEquals("Mantenimiento web", estado.servicios[0].servicio)
    }
}