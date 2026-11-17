package com.example.proyecto_evaluacion.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Etiqueta de color. Cuando cambia el estado, el color pasa suavemente al nuevo,
 * el texto se funde y el tamaño se ajusta con animación.
 */
@Composable
fun EtiquetaColor(
    texto: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val colorAnimado by animateColorAsState(
        targetValue = color,
        animationSpec = tween(450),
        label = "colorEtiqueta"
    )

    Surface(
        color = colorAnimado.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.small,
        modifier = modifier.animateContentSize()
    ) {
        Crossfade(
            targetState = texto,
            animationSpec = tween(300),
            label = "textoEtiqueta"
        ) { textoActual ->
            Text(
                text = textoActual,
                color = colorAnimado,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}