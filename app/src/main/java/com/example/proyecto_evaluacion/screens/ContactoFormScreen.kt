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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_evaluacion.components.CampoValidado
import com.example.proyecto_evaluacion.components.SelectorDesplegable
import com.example.proyecto_evaluacion.ui.theme.colorAdvertencia
import com.example.proyecto_evaluacion.ui.theme.colorExito
import com.example.proyecto_evaluacion.validation.ContactoValidator
import com.example.proyecto_evaluacion.viewmodel.ContactoViewModel

@Composable
fun ContactoFormScreen(
    modifier: Modifier = Modifier,
    viewModel: ContactoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val organizaciones by viewModel.organizaciones.collectAsStateWithLifecycle()
    val contactos by viewModel.contactos.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Usa solo datos ficticios en todos los campos.",
            style = MaterialTheme.typography.bodySmall
        )

        if (organizaciones.isEmpty()) {
            Text(
                text = "No hay organizaciones registradas. Primero registra una desde Inicio.",
                color = colorAdvertencia(),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        SelectorDesplegable(
            label = "Organización",
            seleccion = uiState.organizacionNombre,
            opciones = organizaciones.map { it.nombre },
            onSeleccionar = { indice ->
                val organizacion = organizaciones[indice]
                viewModel.seleccionarOrganizacion(organizacion.id, organizacion.nombre)
            },
            error = uiState.errorOrganizacion
        )

        CampoValidado(
            label = "Nombre del contacto (ficticio)",
            value = uiState.nombre,
            onValueChange = { viewModel.cambiarNombre(it) },
            error = uiState.errorNombre
        )

        CampoValidado(
            label = "Cargo o función",
            value = uiState.cargo,
            onValueChange = { viewModel.cambiarCargo(it) },
            error = uiState.errorCargo
        )

        SelectorDesplegable(
            label = "Área",
            seleccion = uiState.area,
            opciones = ContactoValidator.areasValidas,
            onSeleccionar = { indice ->
                viewModel.cambiarArea(ContactoValidator.areasValidas[indice])
            },
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

        SelectorDesplegable(
            label = "Medio de contacto preferente",
            seleccion = uiState.medio,
            opciones = ContactoValidator.mediosValidos,
            onSeleccionar = { indice ->
                viewModel.cambiarMedio(ContactoValidator.mediosValidos[indice])
            },
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

        Button(
            onClick = { viewModel.guardar() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar contacto")
        }

        HorizontalDivider()

        if (uiState.organizacionId == null) {
            Text(
                text = "Selecciona una organización para ver sus contactos.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(
                text = "Contactos de ${uiState.organizacionNombre} (${contactos.size})",
                style = MaterialTheme.typography.titleMedium
            )

            if (contactos.isEmpty()) {
                Text(
                    text = "Esta organización aún no tiene contactos.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                contactos.forEach { contacto ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = contacto.nombre,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "${contacto.cargo} · ${contacto.area}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${contacto.correo} · ${contacto.telefono}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Prefiere: ${contacto.medioPreferente}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}