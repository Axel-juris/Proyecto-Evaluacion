package com.example.proyecto_evaluacion.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun SelectorDesplegable(
    label: String,
    seleccion: String,
    opciones: List<String>,
    onSeleccionar: (Int) -> Unit,
    error: String?,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = seleccion,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            isError = error != null,
            supportingText = { error?.let { Text(it) } },
            trailingIcon = {
                if (error != null) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Campo con error",
                        tint = MaterialTheme.colorScheme.error
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Abrir lista"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Capa transparente: el campo es de solo lectura, así que el clic se atiende aquí
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expandido = true }
        )

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opciones.forEachIndexed { indice, opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        expandido = false
                        onSeleccionar(indice)
                    }
                )
            }
        }
    }
}