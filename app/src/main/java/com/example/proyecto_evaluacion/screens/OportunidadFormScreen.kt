package com.example.proyecto_evaluacion.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto_evaluacion.validation.OportunidadValidator

@Preview(showBackground = true)
@Composable
fun OportunidadFormScreen(modifier: Modifier = Modifier) {

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var comuna by remember { mutableStateOf("") }
    var requierePropuesta by remember { mutableStateOf(false) }
    var recordatorio by remember { mutableStateOf(true) }

    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorDescripcion by remember { mutableStateOf<String?>(null) }
    var errorComuna by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text("Nueva oportunidad", fontSize = 27.sp)

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                errorNombre = OportunidadValidator.validarNombre(it)
            },
            label = { Text("Nombre de la oportunidad") },
            isError = errorNombre != null,
            supportingText = { errorNombre?.let { Text(it) } },
            trailingIcon = {
                if (errorNombre != null) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Campo con error",
                        tint = MaterialTheme.colorScheme.error
                    )
                } else if (nombre.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Campo válido"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = {
                descripcion = it
                errorDescripcion = OportunidadValidator.validarDescripcion(it)
            },
            label = { Text("Descripción") },
            isError = errorDescripcion != null,
            supportingText = { errorDescripcion?.let { Text(it) } },
            trailingIcon = {
                if (errorDescripcion != null) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Campo con error",
                        tint = MaterialTheme.colorScheme.error
                    )
                } else if (descripcion.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Campo válido"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = comuna,
            onValueChange = {
                comuna = it
                errorComuna = OportunidadValidator.validarComuna(it)
            },
            label = { Text("Comuna") },
            isError = errorComuna != null,
            supportingText = { errorComuna?.let { Text(it) } },
            trailingIcon = {
                if (errorComuna != null) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Campo con error",
                        tint = MaterialTheme.colorScheme.error
                    )
                } else if (comuna.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Campo válido"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = requierePropuesta,
                onCheckedChange = { requierePropuesta = it }
            )
            Text("Requiere propuesta")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Activar recordatorio")
            Switch(
                checked = recordatorio,
                onCheckedChange = { recordatorio = it }
            )
        }

        Button(
            onClick = {
                errorNombre = OportunidadValidator.validarNombre(nombre)
                errorDescripcion = OportunidadValidator.validarDescripcion(descripcion)
                errorComuna = OportunidadValidator.validarComuna(comuna)

                if (errorNombre == null && errorDescripcion == null && errorComuna == null) {
                    println("***** OPORTUNIDAD *****")
                    println("Nombre: $nombre")
                    println("Descripción: $descripcion")
                    println("Comuna: $comuna")
                    println("Requiere propuesta: $requierePropuesta")
                    println("Recordatorio: $recordatorio")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar")
        }
    }
}