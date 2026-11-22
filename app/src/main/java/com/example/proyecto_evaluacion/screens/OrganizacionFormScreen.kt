package com.example.proyecto_evaluacion.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_evaluacion.components.BotonAnimado
import com.example.proyecto_evaluacion.components.CampoValidado
import com.example.proyecto_evaluacion.data.Organizacion
import com.example.proyecto_evaluacion.ui.theme.colorExito
import com.example.proyecto_evaluacion.validation.OrganizacionValidator
import com.example.proyecto_evaluacion.viewmodel.OrganizacionViewModel

@Composable
fun OrganizacionFormScreen(
    modifier: Modifier = Modifier,
    viewModel: OrganizacionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val organizaciones by viewModel.organizaciones.collectAsStateWithLifecycle()

    // Organización que el usuario pidió eliminar y está esperando confirmación
    var organizacionAEliminar by remember { mutableStateOf<Organizacion?>(null) }
    val editando = uiState.organizacionEditandoId != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        AnimatedVisibility(
            visible = editando,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = "Editando organización",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
        }

        CampoValidado(
            label = "Nombre de la organización",
            value = uiState.nombre,
            onValueChange = { viewModel.cambiarNombre(it) },
            error = uiState.errorNombre
        )

        Text(
            text = "Tipos válidos: " + OrganizacionValidator.tiposValidos.joinToString(", "),
            style = MaterialTheme.typography.bodySmall
        )

        CampoValidado(
            label = "Tipo de organización",
            value = uiState.tipo,
            onValueChange = { viewModel.cambiarTipo(it) },
            error = uiState.errorTipo
        )

        CampoValidado(
            label = "Rubro",
            value = uiState.rubro,
            onValueChange = { viewModel.cambiarRubro(it) },
            error = uiState.errorRubro
        )

        CampoValidado(
            label = "Comuna",
            value = uiState.comuna,
            onValueChange = { viewModel.cambiarComuna(it) },
            error = uiState.errorComuna
        )

        CampoValidado(
            label = "Región",
            value = uiState.region,
            onValueChange = { viewModel.cambiarRegion(it) },
            error = uiState.errorRegion
        )

        CampoValidado(
            label = "Sitio web ficticio (opcional)",
            value = uiState.sitioWeb,
            onValueChange = { viewModel.cambiarSitioWeb(it) },
            error = uiState.errorSitioWeb
        )

        CampoValidado(
            label = "Observaciones (opcional)",
            value = uiState.observaciones,
            onValueChange = { viewModel.cambiarObservaciones(it) },
            error = uiState.errorObservaciones
        )

        AnimatedVisibility(
            visible = uiState.mensajeExito != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = uiState.mensajeExito ?: "",
                color = colorExito()
            )
        }

        AnimatedVisibility(
            visible = uiState.errorOperacion != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = uiState.errorOperacion ?: "",
                color = MaterialTheme.colorScheme.error
            )
        }

        BotonAnimado(
            texto = if (editando) "Actualizar organización" else "Guardar organización",
            onClick = { viewModel.guardar() },
            exito = uiState.guardadoExitoso,
            modifier = Modifier.fillMaxWidth()
        )

        if (editando) {
            BotonAnimado(
                texto = "Cancelar edición",
                onClick = { viewModel.cancelarEdicion() },
                contorno = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        HorizontalDivider()

        Text(
            text = "Organizaciones registradas (${organizaciones.size})",
            style = MaterialTheme.typography.titleMedium
        )

        if (organizaciones.isEmpty()) {
            Text(
                text = "Aún no hay organizaciones registradas.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            organizaciones.forEach { organizacion ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = organizacion.nombre,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "${organizacion.tipo} · ${organizacion.rubro}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${organizacion.comuna}, ${organizacion.region}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (organizacion.sitioWeb.isNotBlank()) {
                            Text(
                                text = organizacion.sitioWeb,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { viewModel.editar(organizacion) }) {
                                Text("Editar")
                            }
                            TextButton(onClick = { organizacionAEliminar = organizacion }) {
                                Text("Eliminar", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmación antes de eliminar
    organizacionAEliminar?.let { organizacion ->
        AlertDialog(
            onDismissRequest = { organizacionAEliminar = null },
            title = { Text("¿Eliminar organización?") },
            text = {
                Text("Se eliminará \"${organizacion.nombre}\". Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminar(organizacion)
                        organizacionAEliminar = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { organizacionAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}