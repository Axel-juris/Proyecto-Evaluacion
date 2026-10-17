package com.example.proyecto_evaluacion.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_evaluacion.components.PantallaConBarra
import com.example.proyecto_evaluacion.data.SessionManager
import com.example.proyecto_evaluacion.viewmodel.DashboardViewModel
import com.example.proyecto_evaluacion.viewmodel.ItemDashboard

@Composable
fun InicioScreen(
    onIrALista: () -> Unit,
    onIrAOportunidad: () -> Unit,
    onIrAOrganizacion: () -> Unit,
    onIrAContacto: () -> Unit,
    onIrAPanel: () -> Unit,
    onAbrirDetalle: (Int) -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val usuario by SessionManager.usuarioActual.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val esSupervisor = usuario?.rol == "SUPERVISOR"

    PantallaConBarra(titulo = "Radar de Eventos") { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (esSupervisor) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Hola, ${usuario?.nombre ?: "invitado"}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = if (esSupervisor) "Supervisor Comercial" else "Ejecutivo Comercial",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (uiState.cargando) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Contador("Activas", uiState.totalActivas, Modifier.weight(1f))
                    Contador(
                        titulo = "Atrasadas",
                        valor = uiState.atrasadas.size,
                        modifier = Modifier.weight(1f),
                        destacar = uiState.atrasadas.isNotEmpty()
                    )
                    Contador("Próximos", uiState.proximos.size, Modifier.weight(1f))
                }

                if (esSupervisor) {
                    Text(
                        text = "Oportunidades sin asignar: ${uiState.totalSinAsignar}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                SeccionDashboard(
                    titulo = "Gestiones atrasadas",
                    vacio = "No hay gestiones atrasadas.",
                    items = uiState.atrasadas,
                    onAbrir = onAbrirDetalle
                )
                SeccionDashboard(
                    titulo = "Eventos próximos (14 días)",
                    vacio = "No hay eventos en los próximos 14 días.",
                    items = uiState.proximos,
                    onAbrir = onAbrirDetalle
                )
                SeccionDashboard(
                    titulo = "Seguimientos pendientes",
                    vacio = "No hay seguimientos pendientes.",
                    items = uiState.pendientes,
                    onAbrir = onAbrirDetalle
                )
                SeccionDashboard(
                    titulo = "Oportunidades sin gestión",
                    vacio = "Todas las oportunidades activas tienen gestiones.",
                    items = uiState.sinGestion,
                    onAbrir = onAbrirDetalle
                )
            }

            HorizontalDivider()

            Text(text = "Acciones", style = MaterialTheme.typography.titleMedium)

            Button(onClick = onIrALista, modifier = Modifier.fillMaxWidth()) {
                Text(if (esSupervisor) "Ver todas las oportunidades" else "Mis oportunidades")
            }
            if (esSupervisor) {
                Button(onClick = onIrAPanel, modifier = Modifier.fillMaxWidth()) {
                    Text("Panel del supervisor")
                }
            }
            Button(onClick = onIrAOportunidad, modifier = Modifier.fillMaxWidth()) {
                Text("Nueva oportunidad")
            }
            Button(onClick = onIrAOrganizacion, modifier = Modifier.fillMaxWidth()) {
                Text("Nueva organización")
            }
            Button(onClick = onIrAContacto, modifier = Modifier.fillMaxWidth()) {
                Text("Nuevo contacto")
            }
            OutlinedButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar sesión")
            }
        }
    }
}

@Composable
private fun Contador(
    titulo: String,
    valor: Int,
    modifier: Modifier = Modifier,
    destacar: Boolean = false
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valor.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = if (destacar) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Text(text = titulo, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun SeccionDashboard(
    titulo: String,
    vacio: String,
    items: List<ItemDashboard>,
    onAbrir: (Int) -> Unit
) {
    Text(text = titulo, style = MaterialTheme.typography.titleMedium)

    if (items.isEmpty()) {
        Text(text = vacio, style = MaterialTheme.typography.bodySmall)
    } else {
        items.take(5).forEach { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAbrir(item.oportunidadId) }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = item.titulo, style = MaterialTheme.typography.titleSmall)
                    Text(text = item.organizacion, style = MaterialTheme.typography.bodyMedium)
                    Text(text = item.detalle, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        if (items.size > 5) {
            Text(
                text = "y ${items.size - 5} más en la lista de oportunidades",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}