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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_evaluacion.components.CampoValidado
import com.example.proyecto_evaluacion.validation.ContactoValidator
import com.example.proyecto_evaluacion.viewmodel.ContactoViewModel

@Composable
fun ContactoFormScreen(
    modifier: Modifier = Modifier,
    viewModel: ContactoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Usa solo datos ficticios en todos los campos.",
            fontSize = 12.sp
        )

        CampoValidado(
            label = "Nombre del contacto (ficticio)",
            value = uiState.nombre,
            onValueChange = { viewModel.cambiarNombre(it) },
            error = uiState.errorNombre
        )

        CampoValidado(
            label = "Organización",
            value = uiState.organizacion,
            onValueChange = { viewModel.cambiarOrganizacion(it) },
            error = uiState.errorOrganizacion
        )

        CampoValidado(
            label = "Cargo o función",
            value = uiState.cargo,
            onValueChange = { viewModel.cambiarCargo(it) },
            error = uiState.errorCargo
        )

        Text(
            text = "Áreas válidas: " + ContactoValidator.areasValidas.joinToString(", "),
            fontSize = 12.sp
        )
        CampoValidado(
            label = "Área",
            value = uiState.area,
            onValueChange = { viewModel.cambiarArea(it) },
            error = uiState.errorArea
        )

        CampoValidado(
            label = "Correo (ficticio)",
            value = uiState.correo,
            onValueChange = { viewModel.cambiarCorreo(it) },
            error = uiState.errorCorreo,
            keyboardType = KeyboardType.Email
        )

        CampoValidado(
            label = "Teléfono (ficticio)",
            value = uiState.telefono,
            onValueChange = { viewModel.cambiarTelefono(it) },
            error = uiState.errorTelefono,
            keyboardType = KeyboardType.Phone
        )

        Text(
            text = "Medios válidos: " + ContactoValidator.mediosValidos.joinToString(", "),
            fontSize = 12.sp
        )
        CampoValidado(
            label = "Medio de contacto preferente",
            value = uiState.medio,
            onValueChange = { viewModel.cambiarMedio(it) },
            error = uiState.errorMedio
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
                text = "Contacto guardado correctamente",
                color = MaterialTheme.colorScheme.primary
            )
        }

        Button(
            onClick = { viewModel.guardar() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar contacto")
        }
    }
}