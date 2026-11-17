package com.example.proyecto_evaluacion.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.proyecto_evaluacion.ui.theme.colorExito
import kotlinx.coroutines.delay
import com.example.proyecto_evaluacion.components.BotonAnimado
/**
 * Animaciones reutilizables de la app. Se concentran aquí para que todas las pantallas
 * respondan igual: retroalimentación al tocar, entrada escalonada y estados de botón.
 */

// ---------------------------------------------------------------------------
// Retroalimentación al tocar
// ---------------------------------------------------------------------------

/** Reduce levemente el elemento mientras se mantiene presionado y lo devuelve con un rebote suave. */
@Composable
fun Modifier.escalaAlPresionar(
    interactionSource: InteractionSource,
    escalaPresionado: Float = 0.96f
): Modifier {
    val presionado by interactionSource.collectIsPressedAsState()
    val escala by animateFloatAsState(
        targetValue = if (presionado) escalaPresionado else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "escalaPresion"
    )
    return this.graphicsLayer {
        scaleX = escala
        scaleY = escala
    }
}

/** Equivale a clickable, pero con efecto de escala al presionar. */
@Composable
fun Modifier.clickableAnimado(onClick: () -> Unit): Modifier {
    val interaccion = remember { MutableInteractionSource() }
    return this
        .escalaAlPresionar(interaccion)
        .clickable(
            interactionSource = interaccion,
            indication = LocalIndication.current,
            onClick = onClick
        )
}

// ---------------------------------------------------------------------------
// Entrada escalonada
// ---------------------------------------------------------------------------

/**
 * Hace aparecer su contenido con fundido y desplazamiento hacia arriba, con un retraso
 * proporcional a [indice]. Úsalo en columnas normales (no dentro de LazyColumn).
 */
@Composable
fun AparecerEscalonado(
    indice: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(indice * 90L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 5 }
    ) {
        content()
    }
}

// ---------------------------------------------------------------------------
// Números que cuentan hasta su valor
// ---------------------------------------------------------------------------

/**
 * Devuelve un valor que sube desde 0 hasta [objetivo] al aparecer, y que se anima
 * cada vez que [objetivo] cambia.
 */
@Composable
fun contadorAnimado(objetivo: Int, duracionMs: Int = 900): Int {
    var iniciado by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { iniciado = true }
    val valor by animateIntAsState(
        targetValue = if (iniciado) objetivo else 0,
        animationSpec = tween(duracionMs),
        label = "contador"
    )
    return valor
}

// ---------------------------------------------------------------------------
// Botón con estados: normal -> cargando -> éxito
// ---------------------------------------------------------------------------

private enum class EstadoBoton { NORMAL, CARGANDO, EXITO }

/**
 * Botón con animación de presión y transición entre su texto, un indicador de carga y
 * una marca de éxito (el fondo pasa suavemente al color de éxito).
 * Mientras [cargando] es true el botón queda deshabilitado.
 */
@Composable
fun BotonAnimado(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cargando: Boolean = false,
    exito: Boolean = false,
    habilitado: Boolean = true,
    contorno: Boolean = false
) {
    val interaccion = remember { MutableInteractionSource() }
    val estado = when {
        cargando -> EstadoBoton.CARGANDO
        exito -> EstadoBoton.EXITO
        else -> EstadoBoton.NORMAL
    }

    val contenido: @Composable () -> Unit = {
        AnimatedContent(
            targetState = estado,
            transitionSpec = {
                (fadeIn(tween(220)) + scaleIn(initialScale = 0.7f, animationSpec = tween(220)))
                    .togetherWith(fadeOut(tween(120)))
            },
            label = "contenidoBoton"
        ) { actual ->
            when (actual) {
                EstadoBoton.CARGANDO -> CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                EstadoBoton.EXITO -> Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Guardado"
                )
                EstadoBoton.NORMAL -> Text(texto)
            }
        }
    }

    val botonModifier = modifier.escalaAlPresionar(interaccion)

    if (contorno) {
        OutlinedButton(
            onClick = onClick,
            enabled = habilitado && !cargando,
            interactionSource = interaccion,
            modifier = botonModifier
        ) { contenido() }
    } else {
        val fondo by animateColorAsState(
            targetValue = if (estado == EstadoBoton.EXITO) {
                colorExito()
            } else {
                MaterialTheme.colorScheme.primary
            },
            animationSpec = tween(350),
            label = "fondoBoton"
        )
        Button(
            onClick = onClick,
            enabled = habilitado && !cargando,
            interactionSource = interaccion,
            colors = ButtonDefaults.buttonColors(
                containerColor = fondo,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = botonModifier
        ) { contenido() }
    }
}