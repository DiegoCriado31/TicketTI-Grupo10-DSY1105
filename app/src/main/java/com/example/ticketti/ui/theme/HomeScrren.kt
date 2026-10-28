package com.example.ticketti.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.ticketti.R
import com.example.ticketti.ui.theme.login.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    vm: LoginViewModel = viewModel()
) {

    val state = vm.uiState
    var showPass by remember { mutableStateOf(false) }

    val ColorScheme = darkColorScheme(
        primary = Color(0xFF1565C0),
        onPrimary = Color.White,
        onSurface = Color(0xFF333333),
        onSurfaceVariant = Color(0xFF455A64),
        outline = Color(0xFF78909C)
    )

    MaterialTheme(
        colorScheme = ColorScheme
    ) { // Inicio aplicacion Material

        // Scaffold: permite generar una pantalla basica
        Scaffold(
            containerColor = Color(0xFFDCEBFA),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "TicketTI",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            } // fin topBar
        ) { innerPadding ->
            //innerPadding  es un espacio interno que entrega Scaffold
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) { //inicio inner

                Text(
                    text = "Bienvenido a TicketTI",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Gestión y seguimiento de solicitudes",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter = painterResource(id = R.drawable.logoti),
                    contentDescription = "Logo TicketTI",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentScale = ContentScale.Fit
                )

                OutlinedTextField(
                    value = state.username,
                    onValueChange = vm::onUsernameChange,
                    label = { Text("Usuario") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.95f)
                ) // fin user

                OutlinedTextField(
                    value = state.password,
                    onValueChange = vm::onPasswordChange,
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    singleLine = true,
                    visualTransformation = if (showPass)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPass = !showPass }) {
                            Icon(
                                imageVector = if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPass) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.95f)
                ) //fin pass

                if (state.error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = state.error ?: "",
                        color = Color(0xFFC62828),
                        fontWeight = FontWeight.Bold
                    )
                } // fin state

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        vm.submit { user ->
                            navController.navigate("DrawerMenu/$user") {
                                popUpTo("login") { inclusive = true } // no vuelve al login con back
                                launchSingleTop = true
                            }
                        } // fin submit
                    },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text(if (state.isLoading) "Validando..." else "Iniciar sesión")
                }

            } //fin inner
        } // Fin Scaffold
    } // Final aplicacion Material
} // fin HomeScreen


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() { // Crear un navController de manera ficticia para fines de la vista previa
    val navController = rememberNavController()
    val vm = LoginViewModel()

    HomeScreen(navController = navController, vm = vm)
}