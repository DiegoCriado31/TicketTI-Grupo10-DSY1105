package com.example.ticketti.ui.theme.login

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class LoginViewModelTest {

    @Test
    fun `login con clave incorrecta muestra el error y no navega`() {
        val viewModel = LoginViewModel()
        var navego = false

        viewModel.onUsernameChange("admin")
        viewModel.onPasswordChange("clave-mala")
        viewModel.submit { navego = true }

        assertEquals("Credenciales invalidas", viewModel.uiState.error)
        assertFalse(navego)
        assertFalse(viewModel.uiState.isLoading)
    }
}