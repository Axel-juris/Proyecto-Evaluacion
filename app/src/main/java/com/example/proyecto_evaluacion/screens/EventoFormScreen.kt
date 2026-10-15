package com.example.proyecto_evaluacion.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto_evaluacion.components.CampoValidado
import com.example.proyecto_evaluacion.validation.EventoValidator

@Preview(showBackground = true)
@Composable
fun EventoFormScreen(modifier: Modifier = Modifier) {

    var nombre by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var fechaInicio by remember { mutableStateOf("") }
    var fechaTermino by remember { mutableStateOf("") }
    var horario by remember { mutableStateOf("") }
    var comuna by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var organizacion by remember { mutableStateOf("") }
    var asistentes by remember { mutableStateOf("") }
    var costo by remember { mutableStateOf("") }
    var condiciones by remember { mutableStateOf("") }
    var espaciosDisponibles by remember { mutableStateOf(true) }

    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorTipo by remember { mutableStateOf<String?>(null) }
    var errorFechaInicio by remember { mutableStateOf<String?>(null) }
    var errorFechaTermino by remember { mutableStateOf<String?>(null) }
    var errorHorario by remember { mutableStateOf<String?>(null) }
    var errorComuna by remember { mutableStateOf<String?>(null) }
    var errorRegion by remember { mutableStateOf<String?>(null) }
    var errorOrganizacion by remember { mutableStateOf<String?>(null) }
    var errorAsistentes by remember { mutableStateOf<String?>(null) }
    var errorCosto by remember { mutableStateOf<String?>(null) }
    var errorCondiciones by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text("Nuevo evento", fontSize = 27.sp)

        CampoValidado(
            label = "Nombre del evento",
            value = nombre,
            onValueChange = {
                nombre = it
                errorNombre = EventoValidator.validarNombre(it)
            },
            error = errorNombre
        )

        Text(
            text = "Tipos válidos: " + EventoValidator.tiposValidos.joinToString(", "),
            fontSize = 12.sp
        )
        CampoValidado(
            label = "Tipo de evento",
            value = tipo,
            onValueChange = {
                tipo = it
                errorTipo = EventoValidator.validarTipo(it)
            },
            error = errorTipo
        )

        CampoValidado(
            label = "Fecha de inicio (dd/mm/aaaa)",
            value = fechaInicio,
            onValueChange = {
                fechaInicio = it
                errorFechaInicio = EventoValidator.validarFechaInicio(it)
                if (fechaTermino.isNotEmpty()) {
                    errorFechaTermino = EventoValidator.validarFechaTermino(it, fechaTermino)
                }
            },
            error = errorFechaInicio
        )

        CampoValidado(
            label = "Fecha de término (dd/mm/aaaa)",
            value = fechaTermino,
            onValueChange = {
                fechaTermino = it
                errorFechaTermino = EventoValidator.validarFechaTermino(fechaInicio, it)
            },
            error = errorFechaTermino
        )

        CampoValidado(
            label = "Horario de inicio (HH:mm)",
            value = horario,
            onValueChange = {
                horario = it
                errorHorario = EventoValidator.validarHorario(it)
            },
            error = errorHorario
        )

        CampoValidado(
            label = "Comuna",
            value = comuna,
            onValueChange = {
                comuna = it
                errorComuna = EventoValidator.validarComuna(it)
            },
            error = errorComuna
        )

        CampoValidado(
            label = "Región",
            value = region,
            onValueChange = {
                region = it
                errorRegion = EventoValidator.validarRegion(it)
            },
            error = errorRegion
        )

        CampoValidado(
            label = "Organización responsable",
            value = organizacion,
            onValueChange = {
                organizacion = it
                errorOrganizacion = EventoValidator.validarOrganizacion(it)
            },
            error = errorOrganizacion
        )

        CampoValidado(
            label = "Público esperado (cantidad de asistentes)",
            value = asistentes,
            onValueChange = {
                asistentes = it
                errorAsistentes = EventoValidator.validarAsistentes(it)
            },
            error = errorAsistentes,
            keyboardType = KeyboardType.Number
        )

        CampoValidado(
            label = "Costo referencial en pesos (ficticio)",
            value = costo,
            onValueChange = {
                costo = it
                errorCosto = EventoValidator.validarCosto(it)
            },
            error = errorCosto,
            keyboardType = KeyboardType.Number
        )

        CampoValidado(
            label = "Condiciones de participación (opcional)",
            value = condiciones,
            onValueChange = {
                condiciones = it
                errorCondiciones = EventoValidator.validarCondiciones(it)
            },
            error = errorCondiciones
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Espacios disponibles")
            Switch(
                checked = espaciosDisponibles,
                onCheckedChange = { espaciosDisponibles = it }
            )
        }

        Button(
            onClick = {
                errorNombre = EventoValidator.validarNombre(nombre)
                errorTipo = EventoValidator.validarTipo(tipo)
                errorFechaInicio = EventoValidator.validarFechaInicio(fechaInicio)
                errorFechaTermino = EventoValidator.validarFechaTermino(fechaInicio, fechaTermino)
                errorHorario = EventoValidator.validarHorario(horario)
                errorComuna = EventoValidator.validarComuna(comuna)
                errorRegion = EventoValidator.validarRegion(region)
                errorOrganizacion = EventoValidator.validarOrganizacion(organizacion)
                errorAsistentes = EventoValidator.validarAsistentes(asistentes)
                errorCosto = EventoValidator.validarCosto(costo)
                errorCondiciones = EventoValidator.validarCondiciones(condiciones)

                val todoValido = listOf(
                    errorNombre, errorTipo, errorFechaInicio, errorFechaTermino,
                    errorHorario, errorComuna, errorRegion, errorOrganizacion,
                    errorAsistentes, errorCosto, errorCondiciones
                ).all { it == null }

                if (todoValido) {
                    println("***** EVENTO *****")
                    println("Nombre: $nombre")
                    println("Tipo: $tipo")
                    println("Inicio: $fechaInicio")
                    println("Término: $fechaTermino")
                    println("Horario: $horario")
                    println("Comuna: $comuna")
                    println("Región: $region")
                    println("Organización: $organizacion")
                    println("Asistentes: $asistentes")
                    println("Costo: $costo")
                    println("Condiciones: $condiciones")
                    println("Espacios disponibles: $espaciosDisponibles")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar evento")
        }
    }
}