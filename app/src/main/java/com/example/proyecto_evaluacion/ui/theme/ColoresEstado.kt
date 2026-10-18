package com.example.proyecto_evaluacion.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colores semánticos centralizados. Cada uno tiene una variante para tema claro
// y otra más luminosa para tema oscuro, así se leen bien en ambos.
private val NuevaClaro = Color(0xFF1565C0)
private val NuevaOscuro = Color(0xFF90CAF9)
private val EnGestionClaro = Color(0xFF8A5A00)
private val EnGestionOscuro = Color(0xFFFFCC80)
private val ConfirmadaClaro = Color(0xFF2E7D32)
private val ConfirmadaOscuro = Color(0xFFA5D6A7)
private val DescartadaClaro = Color(0xFF616161)
private val DescartadaOscuro = Color(0xFFBDBDBD)

private val AltaClaro = Color(0xFFC62828)
private val AltaOscuro = Color(0xFFEF9A9A)
private val MediaClaro = Color(0xFFB45F06)
private val MediaOscuro = Color(0xFFFFAB91)
private val BajaClaro = Color(0xFF2E7D32)
private val BajaOscuro = Color(0xFFA5D6A7)

private val ExitoClaro = Color(0xFF2E7D32)
private val ExitoOscuro = Color(0xFFA5D6A7)
private val AdvertenciaClaro = Color(0xFF8A5A00)
private val AdvertenciaOscuro = Color(0xFFFFCC80)

@Composable
private fun elegir(claro: Color, oscuro: Color): Color =
    if (isSystemInDarkTheme()) oscuro else claro

@Composable
fun colorEstado(estado: String): Color = when (estado) {
    "NUEVA" -> elegir(NuevaClaro, NuevaOscuro)
    "EN_GESTION" -> elegir(EnGestionClaro, EnGestionOscuro)
    "CONFIRMADA" -> elegir(ConfirmadaClaro, ConfirmadaOscuro)
    else -> elegir(DescartadaClaro, DescartadaOscuro)
}

@Composable
fun colorPrioridad(prioridad: String): Color = when (prioridad) {
    "ALTA" -> elegir(AltaClaro, AltaOscuro)
    "MEDIA" -> elegir(MediaClaro, MediaOscuro)
    else -> elegir(BajaClaro, BajaOscuro)
}

// Mensajes de operación exitosa
@Composable
fun colorExito(): Color = elegir(ExitoClaro, ExitoOscuro)

// Avisos que no son errores (por ejemplo, falta de datos previos)
@Composable
fun colorAdvertencia(): Color = elegir(AdvertenciaClaro, AdvertenciaOscuro)

// El color de error sale del tema: MaterialTheme.colorScheme.error

fun textoEstado(estado: String): String = when (estado) {
    "NUEVA" -> "Nueva"
    "EN_GESTION" -> "En gestión"
    "CONFIRMADA" -> "Confirmada"
    "DESCARTADA" -> "Descartada"
    else -> estado
}