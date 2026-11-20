package com.example.proyecto_evaluacion.screens

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
import com.example.proyecto_evaluacion.components.AparecerEscalonado
import com.example.proyecto_evaluacion.components.BotonAnimado
import com.example.proyecto_evaluacion.components.PantallaConBarra
import com.example.proyecto_evaluacion.components.clickableAnimado
import com.example.proyecto_evaluacion.components.contadorAnimado
import com.example.proyecto_evaluacion.data.SessionManager
import com.example.proyecto_evaluacion.fechas.FeriadosHelper
import com.example.proyecto_evaluacion.viewmodel.DashboardViewModel
import com.example.proyecto_evaluacion.viewmodel.FeriadosUiState
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
    val feriados by viewModel.feriados.collectAsStateWithLifecycle()
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

            AparecerEscalonado(indice = 0) {
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
                    indice = 1,
                    titulo = "Gestiones atrasadas",
                    vacio = "No hay gestiones atrasadas.",
                    items = uiState.atrasadas,
                    onAbrir = onAbrirDetalle
                )
                SeccionDashboard(
                    indice = 2,
                    titulo = "Eventos próximos (14 días)",
                    vacio = "No hay eventos en los próximos 14 días.",
                    items = uiState.proximos,
                    onAbrir = onAbrirDetalle
                )
                SeccionDashboard(
                    indice = 3,
                    titulo = "Seguimientos pendientes",
                    vacio = "No hay seguimientos pendientes.",
                    items = uiState.pendientes,
                    onAbrir = onAbrirDetalle
                )
                SeccionDashboard(
                    indice = 4,
                    titulo = "Oportunidades sin gestión",
                    vacio = "Todas las oportunidades activas tienen gestiones.",
                    items = uiState.sinGestion,
                    onAbrir = onAbrirDetalle
                )
            }

            SeccionFeriados(feriados)

            HorizontalDivider()

            Text(text = "Acciones", style = MaterialTheme.typography.titleMedium)

            BotonAnimado(
                texto = if (esSupervisor) "Ver todas las oportunidades" else "Mis oportunidades",
                onClick = onIrALista,
                modifier = Modifier.fillMaxWidth()
            )
            if (esSupervisor) {
                BotonAnimado(
                    texto = "Panel del supervisor",
                    onClick = onIrAPanel,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            BotonAnimado(
                texto = "Nueva oportunidad",
                onClick = onIrAOportunidad,
                modifier = Modifier.fillMaxWidth()
            )
            BotonAnimado(
                texto = "Nueva organización",
                onClick = onIrAOrganizacion,
                modifier = Modifier.fillMaxWidth()
            )
            BotonAnimado(
                texto = "Nuevo contacto",
                onClick = onIrAContacto,
                modifier = Modifier.fillMaxWidth()
            )
            BotonAnimado(
                texto = "Cerrar sesión",
                onClick = onCerrarSesion,
                contorno = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SeccionFeriados(estado: FeriadosUiState) {
    // Copia local: así Kotlin puede confirmar que no es nula dentro del when
    val error = estado.error

    AparecerEscalonado(indice = 5) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Próximos feriados en Chile", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Útil para evitar agendar eventos en días no hábiles.",
                style = MaterialTheme.typography.bodySmall
            )

            when {
                estado.cargando -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

                error != null -> Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )

                estado.feriados.isEmpty() -> Text(
                    text = "No hay feriados próximos.",
                    style = MaterialTheme.typography.bodySmall
                )

                else -> estado.feriados.forEach { feriado ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = feriado.localName,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = FeriadosHelper.formatear(feriado.date),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
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
                text = contadorAnimado(valor).toString(),
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
    indice: Int,
    titulo: String,
    vacio: String,
    items: List<ItemDashboard>,
    onAbrir: (Int) -> Unit
) {
    AparecerEscalonado(indice = indice) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = titulo, style = MaterialTheme.typography.titleMedium)

            if (items.isEmpty()) {
                Text(text = vacio, style = MaterialTheme.typography.bodySmall)
            } else {
                items.take(5).forEach { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickableAnimado { onAbrir(item.oportunidadId) }
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
    }
}