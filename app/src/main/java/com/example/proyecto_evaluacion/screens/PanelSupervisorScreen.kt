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
import com.example.proyecto_evaluacion.components.EtiquetaColor
import com.example.proyecto_evaluacion.ui.theme.colorEstado
import com.example.proyecto_evaluacion.ui.theme.textoEstado
import com.example.proyecto_evaluacion.viewmodel.PanelSupervisorViewModel

@Composable
fun PanelSupervisorScreen(
    modifier: Modifier = Modifier,
    onAbrirDetalle: (Int) -> Unit = {},
    viewModel: PanelSupervisorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.cargando) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (!uiState.autorizado) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Acceso restringido. Esta sección es solo para el supervisor comercial.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Total de oportunidades: ${uiState.totalOportunidades}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    listOf("NUEVA", "EN_GESTION", "CONFIRMADA", "DESCARTADA").forEach { estado ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EtiquetaColor(
                                texto = textoEstado(estado),
                                color = colorEstado(estado)
                            )
                            Text(
                                text = "${uiState.porEstado[estado] ?: 0}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Text(text = "Carga por ejecutivo", style = MaterialTheme.typography.titleMedium)

            if (uiState.cargaEjecutivos.isEmpty()) {
                Text(
                    text = "No hay ejecutivos registrados.",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                uiState.cargaEjecutivos.forEach { carga ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = carga.nombre, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "${carga.activas} activas · ${carga.total} en total",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            Text(
                text = "Sin asignar (${uiState.sinAsignar.size})",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Toca una oportunidad para asignarle un responsable.",
                style = MaterialTheme.typography.bodySmall
            )

            if (uiState.sinAsignar.isEmpty()) {
                Text(
                    text = "Todas las oportunidades tienen responsable.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                uiState.sinAsignar.forEach { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAbrirDetalle(item.oportunidadId) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = item.titulo, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = item.organizacion,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(text = item.detalle, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}