package com.example.ticketti.ui.solicitud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// PANTALLA PROVISORIA: se reemplaza por el formulario real (validaciones y camara).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudFormScreen(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Nueva solicitud") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Aquí irá el formulario: título, descripción, categoría, servicio y foto de evidencia.")

            Button(onClick = { navController.popBackStack() }) {
                Text("Volver")
            }
        }
    }
}