package com.example.proyecto_evaluacion.navegacion

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyecto_evaluacion.components.PantallaConBarra
import com.example.proyecto_evaluacion.data.SessionManager
import com.example.proyecto_evaluacion.screens.ContactoFormScreen
import com.example.proyecto_evaluacion.screens.DetalleOportunidadScreen
import com.example.proyecto_evaluacion.screens.InicioScreen
import com.example.proyecto_evaluacion.screens.ListaOportunidadesScreen
import com.example.proyecto_evaluacion.screens.LoginScreen
import com.example.proyecto_evaluacion.screens.OportunidadFormScreen
import com.example.proyecto_evaluacion.screens.OrganizacionFormScreen
import com.example.proyecto_evaluacion.screens.PanelSupervisorScreen

@Composable
fun Navegacion() {
    val navController = rememberNavController()

    val usuario by SessionManager.usuarioActual.collectAsStateWithLifecycle()
    val rutaActual = navController.currentBackStackEntryAsState().value?.destination?.route

    // Control de sesión: si no hay usuario activo (por ejemplo, el sistema cerró la app en
    // segundo plano y la restauró en una pantalla interna), se vuelve al inicio de sesión
    LaunchedEffect(usuario, rutaActual) {
        if (usuario == null && rutaActual != null && rutaActual != "login") {
            navController.navigate("login") {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

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
                onIrAPanel = { navController.navigate("panel") },
                onAbrirDetalle = { id -> navController.navigate("detalle/$id") },
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
                    onNuevaOportunidad = { navController.navigate("oportunidad") },
                    onAbrirDetalle = { id -> navController.navigate("detalle/$id") }
                )
            }
        }

        composable("panel") {
            PantallaConBarra(
                titulo = "Panel del supervisor",
                onVolver = { navController.popBackStack() }
            ) { innerPadding ->
                PanelSupervisorScreen(
                    modifier = Modifier.padding(innerPadding),
                    onAbrirDetalle = { id -> navController.navigate("detalle/$id") }
                )
            }
        }

        composable(
            route = "detalle/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            PantallaConBarra(
                titulo = "Detalle de oportunidad",
                onVolver = { navController.popBackStack() }
            ) { innerPadding ->
                DetalleOportunidadScreen(
                    oportunidadId = id,
                    modifier = Modifier.padding(innerPadding),
                    onEditar = { oportunidadId -> navController.navigate("editar/$oportunidadId") }
                )
            }
        }

        composable(
            route = "editar/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            PantallaConBarra(
                titulo = "Editar oportunidad",
                onVolver = { navController.popBackStack() }
            ) { innerPadding ->
                OportunidadFormScreen(
                    modifier = Modifier.padding(innerPadding),
                    oportunidadId = id,
                    onGuardado = { navController.popBackStack() }
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