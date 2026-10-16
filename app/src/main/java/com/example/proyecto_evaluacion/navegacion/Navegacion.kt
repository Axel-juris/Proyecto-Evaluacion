package com.example.proyecto_evaluacion.navegacion

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyecto_evaluacion.components.PantallaConBarra
import com.example.proyecto_evaluacion.data.SessionManager
import com.example.proyecto_evaluacion.screens.ContactoFormScreen
import com.example.proyecto_evaluacion.screens.InicioScreen
import com.example.proyecto_evaluacion.screens.ListaOportunidadesScreen
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
                onIrALista = { navController.navigate("lista") },
                onIrAOportunidad = { navController.navigate("oportunidad") },
                onIrAOrganizacion = { navController.navigate("organizacion") },
                onIrAContacto = { navController.navigate("contacto") },
                onCerrarSesion = {
                    SessionManager.cerrarSesion()
                    navController.navigate("login") {
                        popUpTo("inicio") { inclusive = true }
                    }
                }
            )
        }

        composable("lista") {
            PantallaConBarra(
                titulo = "Oportunidades",
                onVolver = { navController.popBackStack() }
            ) { innerPadding ->
                ListaOportunidadesScreen(
                    modifier = Modifier.padding(innerPadding),
                    onNuevaOportunidad = { navController.navigate("oportunidad") }
                )
            }
        }

        composable("oportunidad") {
            PantallaConBarra(
                titulo = "Nueva oportunidad",
                onVolver = { navController.popBackStack() }
            ) { innerPadding ->
                OportunidadFormScreen(modifier = Modifier.padding(innerPadding))
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

        composable("contacto") {
            PantallaConBarra(
                titulo = "Nuevo contacto",
                onVolver = { navController.popBackStack() }
            ) { innerPadding ->
                ContactoFormScreen(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}