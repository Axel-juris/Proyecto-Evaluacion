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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_evaluacion.components.CampoValidado
import com.example.proyecto_evaluacion.validation.OrganizacionValidator
import com.example.proyecto_evaluacion.viewmodel.OrganizacionViewModel

@Composable
fun OrganizacionFormScreen(
    modifier: Modifier = Modifier,
    viewModel: OrganizacionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        CampoValidado(
            label = "Nombre de la organización",
            value = uiState.nombre,
            onValueChange = { viewModel.cambiarNombre(it) },
            error = uiState.errorNombre
        )

        Text(
            text = "Tipos válidos: " + OrganizacionValidator.tiposValidos.joinToString(", "),
            fontSize = 12.sp
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
            visible = uiState.guardadoExitoso,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = "Organización guardada correctamente",
                color = MaterialTheme.colorScheme.primary
            )
        }

        Button(
            onClick = { viewModel.guardar() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar organización")
        }
    }
}
