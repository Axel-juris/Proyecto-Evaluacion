package com.example.proyecto_evaluacion.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun EtiquetaColor(
    texto: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Text(
            text = texto,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
