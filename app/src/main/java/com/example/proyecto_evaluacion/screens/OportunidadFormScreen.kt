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
import com.example.proyecto_evaluacion.data.SessionManager
import com.example.proyecto_evaluacion.validation.EventoValidator
import com.example.proyecto_evaluacion.validation.OportunidadValidator
import com.example.proyecto_evaluacion.viewmodel.OportunidadViewModel

@Composable
fun OportunidadFormScreen(
    modifier: Modifier = Modifier,
    viewModel: OportunidadViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val organizaciones by viewModel.organizaciones.collectAsStateWithLifecycle()
    val contactos by viewModel.contactos.collectAsStateWithLifecycle()
    val usuario by SessionManager.usuarioActual.collectAsStateWithLifecycle()
    val esSupervisor = usuario?.rol == "SUPERVISOR"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = if (esSupervisor) {
                "Quedará sin asignar hasta que la asignes a un ejecutivo."
            } else {
                "Esta oportunidad quedará asignada a ti."
            },
            style = MaterialTheme.typography.bodySmall
        )

        if (organizaciones.isEmpty()) {
            Text(
                text = "No hay organizaciones registradas. Primero registra una desde Inicio.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        CampoValidado(
            label = "Nombre de la oportunidad",
            value = uiState.titulo,
            onValueChange = { viewModel.cambiarTitulo(it) },
            error = uiState.errorTitulo
        )

        CampoValidado(
            label = "Descripción",
            value = uiState.descripcion,
            onValueChange = { viewModel.cambiarDescripcion(it) },
            error = uiState.errorDescripcion
        )

        SelectorDesplegable(
            label = "Tipo de evento",
            seleccion = uiState.tipoEvento,
            opciones = EventoValidator.tiposValidos,
            onSeleccionar = { indice ->
                viewModel.cambiarTipo(EventoValidator.tiposValidos[indice])
            },
            error = uiState.errorTipo
        )

        SelectorDesplegable(
            label = "Prioridad",
            seleccion = uiState.prioridad,
            opciones = OportunidadValidator.prioridades,
            onSeleccionar = { indice ->
                viewModel.cambiarPrioridad(OportunidadValidator.prioridades[indice])
            },
            error = uiState.errorPrioridad
        )

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

        if (uiState.organizacionId != null) {
            val opcionesContacto = listOf("Sin contacto") +
                    contactos.map { "${it.nombre} (${it.cargo})" }

            SelectorDesplegable(
                label = "Contacto (opcional)",
                seleccion = uiState.contactoNombre,
                opciones = opcionesContacto,
                onSeleccionar = { indice ->
                    if (indice == 0) {
                        viewModel.seleccionarContacto(null, "Sin contacto")
                    } else {
                        val contacto = contactos[indice - 1]
                        viewModel.seleccionarContacto(
                            contacto.id,
                            "${contacto.nombre} (${contacto.cargo})"
                        )
                    }
                },
                error = null
            )

            if (contactos.isEmpty()) {
                Text(
                    text = "Esta organización aún no tiene contactos registrados.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        CampoValidado(
            label = "Fecha de inicio (dd/mm/aaaa)",
            value = uiState.fechaInicio,
            onValueChange = { viewModel.cambiarFechaInicio(it) },
            error = uiState.errorFechaInicio
        )

        CampoValidado(
            label = "Fecha de término (dd/mm/aaaa)",
            value = uiState.fechaTermino,
            onValueChange = { viewModel.cambiarFechaTermino(it) },
            error = uiState.errorFechaTermino
        )

        CampoValidado(
            label = "Horario de inicio (HH:mm)",
            value = uiState.horario,
            onValueChange = { viewModel.cambiarHorario(it) },
            error = uiState.errorHorario
        )

        CampoValidado(
            label = "Lugar del evento (comuna y región)",
            value = uiState.lugar,
            onValueChange = { viewModel.cambiarLugar(it) },
            error = uiState.errorLugar
        )

        CampoValidado(
            label = "Público esperado (cantidad de asistentes)",
            value = uiState.publico,
            onValueChange = { viewModel.cambiarPublico(it) },
            error = uiState.errorPublico,
            keyboardType = KeyboardType.Number
        )

        CampoValidado(
            label = "Costo referencial en pesos (ficticio)",
            value = uiState.costo,
            onValueChange = { viewModel.cambiarCosto(it) },
            error = uiState.errorCosto,
            keyboardType = KeyboardType.Number
        )

        AnimatedVisibility(
            visible = uiState.guardadoExitoso,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = "Oportunidad guardada correctamente",
                color = MaterialTheme.colorScheme.primary
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
            Text("Guardar oportunidad")
        }
    }
}