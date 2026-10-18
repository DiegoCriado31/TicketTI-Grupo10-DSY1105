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

// PANTALLA PROVISORIA: se reemplaza por la version real (lista desde Room).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaSolicitudesScreen(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Mis solicitudes") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Aquí irá la lista de solicitudes guardadas en Room.")

            Button(onClick = { navController.navigate("solicitud_form") }) {
                Text("Nueva solicitud")
            }

            Button(onClick = { navController.popBackStack() }) {
                Text("Volver")
            }
        }
    }
}