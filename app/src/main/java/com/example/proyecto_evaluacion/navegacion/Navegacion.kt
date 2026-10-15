package com.example.proyecto_evaluacion.navegacion

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyecto_evaluacion.components.PantallaConBarra
import com.example.proyecto_evaluacion.screens.EventoFormScreen
import com.example.proyecto_evaluacion.screens.InicioScreen
import com.example.proyecto_evaluacion.screens.LoginScreen
import com.example.proyecto_evaluacion.screens.OportunidadFormScreen
import com.example.proyecto_evaluacion.screens.OrganizacionFormScreen

@Composable
fun Navegacion() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {
            Scaffold { innerPadding ->
                LoginScreen(
                    modifier = Modifier.padding(innerPadding),
                    onLoginExitoso = {
                        navController.navigate("inicio") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }
        }

        composable("inicio") {
            InicioScreen(
                onIrAOportunidad = { navController.navigate("oportunidad") },
                onIrAEvento = { navController.navigate("evento") },
                onIrAOrganizacion = { navController.navigate("organizacion") },
                onCerrarSesion = {
                    navController.navigate("login") {
                        popUpTo("inicio") { inclusive = true }
                    }
                }
            )
        }

        composable("oportunidad") {
            PantallaConBarra(
                titulo = "Nueva oportunidad",
                onVolver = { navController.popBackStack() }
            ) { innerPadding ->
                OportunidadFormScreen(modifier = Modifier.padding(innerPadding))
            }
        }

        composable("evento") {
            PantallaConBarra(
                titulo = "Nuevo evento",
                onVolver = { navController.popBackStack() }
            ) { innerPadding ->
                EventoFormScreen(modifier = Modifier.padding(innerPadding))
            }
        }

        composable("organizacion") {
            PantallaConBarra(
                titulo = "Nueva organización",
                onVolver = { navController.popBackStack() }
            ) { innerPadding ->
                OrganizacionFormScreen(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}