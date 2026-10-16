package com.example.proyecto_evaluacion.ui.theme

import androidx.compose.ui.graphics.Color

// Colores semánticos centralizados: las pantallas no usan hexadecimales
val EstadoNueva = Color(0xFF1E88E5)
val EstadoEnGestion = Color(0xFF9A6700)
val EstadoConfirmada = Color(0xFF2E7D32)
val EstadoDescartada = Color(0xFF757575)

val PrioridadAlta = Color(0xFFC62828)
val PrioridadMedia = Color(0xFFB45F06)
val PrioridadBaja = Color(0xFF2E7D32)

fun colorEstado(estado: String): Color = when (estado) {
    "NUEVA" -> EstadoNueva
    "EN_GESTION" -> EstadoEnGestion
    "CONFIRMADA" -> EstadoConfirmada
    "DESCARTADA" -> EstadoDescartada
    else -> EstadoDescartada
}

fun colorPrioridad(prioridad: String): Color = when (prioridad) {
    "ALTA" -> PrioridadAlta
    "MEDIA" -> PrioridadMedia
    "BAJA" -> PrioridadBaja
    else -> PrioridadBaja
}

fun textoEstado(estado: String): String = when (estado) {
    "NUEVA" -> "Nueva"
    "EN_GESTION" -> "En gestión"
    "CONFIRMADA" -> "Confirmada"
    "DESCARTADA" -> "Descartada"
    else -> estado
}