package com.example.ticketti.ui.solicitud

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EstadoEtiquetaTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun muestraElNombreDelEstado() {
        composeRule.setContent { EstadoEtiqueta("Resuelta") }

        composeRule.onNodeWithText("Resuelta").assertIsDisplayed()
    }
}
