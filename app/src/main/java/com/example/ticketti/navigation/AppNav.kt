package com.example.ticketti.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ticketti.ui.solicitud.ListaSolicitudesScreen
import com.example.ticketti.ui.solicitud.SolicitudFormScreen
import com.example.ticketti.ui.theme.HomeScreen
import com.example.ticketti.view.DrawerMenu

@Composable
fun AppNav() {
    val navController = rememberNavController()

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
            DrawerMenu(username = username, navController = navController)
        }

        // Lista de solicitudes (pantalla provisoria: la reemplaza la version real)
        composable("lista_solicitudes") {
            ListaSolicitudesScreen(navController = navController)
        }

        // Formulario de solicitud (pantalla provisoria: la reemplaza la version real)
        composable("solicitud_form") {
            SolicitudFormScreen(navController = navController)
        }
    }
}