package com.example.ticketti.ui.solicitud

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ticketti.data.local.SolicitudEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaSolicitudesScreen(
    navController: NavController,
    viewModel: SolicitudViewModel
) {
    val solicitudes by viewModel.solicitudes.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mis solicitudes") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("solicitud_form") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva solicitud")
            }
        }
    ) { innerPadding ->
        if (solicitudes.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No hay solicitudes registradas",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Text(
                    text = "Presiona + para agregar una nueva",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(solicitudes) { solicitud ->
                    SolicitudItem(
                        solicitud = solicitud,
                        onClick = {
                            navController.navigate("solicitud_form/${solicitud.id}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SolicitudItem(
    solicitud: SolicitudEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = solicitud.codigo.ifEmpty { "TI-0000" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = solicitud.estado,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = solicitud.titulo,
                style = MaterialTheme.typography.titleLarge
            )

            Row {
                Text(
                    text = "Categoría:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = solicitud.categoria,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}