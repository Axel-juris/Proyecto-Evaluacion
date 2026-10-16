package com.example.proyecto_evaluacion.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_evaluacion.components.EtiquetaColor
import com.example.proyecto_evaluacion.data.SessionManager
import com.example.proyecto_evaluacion.ui.theme.colorEstado
import com.example.proyecto_evaluacion.ui.theme.colorPrioridad
import com.example.proyecto_evaluacion.ui.theme.textoEstado
import com.example.proyecto_evaluacion.viewmodel.ListaOportunidadesViewModel
import com.example.proyecto_evaluacion.viewmodel.OportunidadItem
import com.example.proyecto_evaluacion.viewmodel.TODOS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaOportunidadesScreen(
    modifier: Modifier = Modifier,
    onNuevaOportunidad: () -> Unit = {},
    onAbrirDetalle: (Int) -> Unit = {},
    viewModel: ListaOportunidadesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filtros by viewModel.filtros.collectAsStateWithLifecycle()
    val usuario by SessionManager.usuarioActual.collectAsStateWithLifecycle()
    val esSupervisor = usuario?.rol == "SUPERVISOR"

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = if (esSupervisor) {
                "Todas las oportunidades (${uiState.totalSinFiltrar})"
            } else {
                "Mis oportunidades (${uiState.totalSinFiltrar})"
            },
            style = MaterialTheme.typography.titleMedium
        )

        Button(onClick = onNuevaOportunidad, modifier = Modifier.fillMaxWidth()) {
            Text("Nueva oportunidad")
        }

        OutlinedTextField(
            value = filtros.busqueda,
            onValueChange = { viewModel.cambiarBusqueda(it) },
            label = { Text("Buscar por nombre u organización") },
            singleLine = true,
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
            },
            trailingIcon = {
                if (filtros.busqueda.isNotEmpty()) {
                    IconButton(onClick = { viewModel.cambiarBusqueda("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Limpiar búsqueda"
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        FilaFiltros(
            titulo = "Estado",
            opciones = listOf(
                TODOS to "Todos",
                "NUEVA" to "Nueva",
                "EN_GESTION" to "En gestión",
                "CONFIRMADA" to "Confirmada",
                "DESCARTADA" to "Descartada"
            ),
            seleccionado = filtros.estado,
            onSeleccionar = { viewModel.cambiarEstado(it) }
        )

        FilaFiltros(
            titulo = "Prioridad",
            opciones = listOf(
                TODOS to "Todas",
                "ALTA" to "Alta",
                "MEDIA" to "Media",
                "BAJA" to "Baja"
            ),
            seleccionado = filtros.prioridad,
            onSeleccionar = { viewModel.cambiarPrioridad(it) }
        )

        when {
            uiState.cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.totalSinFiltrar == 0 -> {
                Text(
                    text = if (esSupervisor) {
                        "No hay oportunidades registradas todavía."
                    } else {
                        "Aún no tienes oportunidades asignadas. Crea una con el botón de arriba."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            uiState.items.isEmpty() -> {
                Text(
                    text = "Ninguna oportunidad coincide con la búsqueda o los filtros.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedButton(onClick = { viewModel.limpiarFiltros() }) {
                    Text("Limpiar filtros")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.items, key = { it.oportunidad.id }) { item ->
                        TarjetaOportunidad(
                            item = item,
                            esSupervisor = esSupervisor,
                            onClick = { onAbrirDetalle(item.oportunidad.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaFiltros(
    titulo: String,
    opciones: List<Pair<String, String>>,
    seleccionado: String,
    onSeleccionar: (String) -> Unit
) {
    Column {
        Text(text = titulo, style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            opciones.forEach { (valor, etiqueta) ->
                FilterChip(
                    selected = seleccionado == valor,
                    onClick = { onSeleccionar(valor) },
                    label = { Text(etiqueta) }
                )
            }
        }
    }
}

@Composable
private fun TarjetaOportunidad(
    item: OportunidadItem,
    esSupervisor: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val oportunidad = item.oportunidad

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = oportunidad.titulo, style = MaterialTheme.typography.titleMedium)
            Text(text = item.organizacionNombre, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "${oportunidad.tipoEvento} · ${oportunidad.fechaEvento} ${oportunidad.horario}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(text = oportunidad.lugarEvento, style = MaterialTheme.typography.bodySmall)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EtiquetaColor(
                    texto = textoEstado(oportunidad.estado),
                    color = colorEstado(oportunidad.estado)
                )
                EtiquetaColor(
                    texto = "Prioridad ${oportunidad.prioridad.lowercase()}",
                    color = colorPrioridad(oportunidad.prioridad)
                )
            }

            if (esSupervisor) {
                Text(
                    text = "Responsable: ${item.responsable}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
