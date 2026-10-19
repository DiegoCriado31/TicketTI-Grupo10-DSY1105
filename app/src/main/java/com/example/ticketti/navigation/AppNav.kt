package com.example.ticketti.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ticketti.ui.solicitud.ListaSolicitudesScreen
import com.example.ticketti.ui.solicitud.SolicitudFormScreen
import com.example.ticketti.ui.solicitud.SolicitudViewModel
import com.example.ticketti.ui.theme.HomeScreen
import com.example.ticketti.view.DrawerMenu
import androidx.compose.runtime.LaunchedEffect

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val solicitudViewModel: SolicitudViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {

        // Login
        composable("login") {
            HomeScreen(navController = navController)
        }

        // Menu principal
        composable(
            route = "DrawerMenu/{username}",
            arguments = listOf(
                navArgument("username") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username").orEmpty()
            LaunchedEffect(username) {
                solicitudViewModel.iniciarSesion(username)
            }
            DrawerMenu(username = username, navController = navController)
        }

        // Lista de solicitudes
        composable("lista_solicitudes") {
            ListaSolicitudesScreen(navController = navController, viewModel = solicitudViewModel)
        }

        // Formulario de solicitud
        composable(
            route = "solicitud_form/{solicitudId}",
            arguments = listOf(
                navArgument("solicitudId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val solicitudId = backStackEntry.arguments?.getLong("solicitudId") ?: 0L
            SolicitudFormScreen(
                navController = navController,
                viewModel = solicitudViewModel,
                solicitudId = if (solicitudId == 0L) null else solicitudId
            )
        }

        // Ruta alternativa para nueva solicitud
        composable("solicitud_form") {
            SolicitudFormScreen(
                navController = navController,
                viewModel = solicitudViewModel,
                solicitudId = null
            )
        }
    }
}