package com.example.proyecto_evaluacion.validation

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SeguimientoValidator {

    // Código que se guarda en Room y etiqueta que ve el usuario
    val tiposGestion = listOf(
        "LLAMADA" to "Llamada",
        "REUNION" to "Reunión",
        "CORREO" to "Correo",
        "VISITA" to "Visita"
    )

    fun etiquetaTipo(codigo: String): String =
        tiposGestion.firstOrNull { it.first == codigo }?.second ?: codigo

    private fun parsearFecha(fecha: String): Date? {
        if (!Regex("^\\d{2}/\\d{2}/\\d{4}$").matches(fecha)) return null
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        formato.isLenient = false
        return try {
            formato.parse(fecha)
        } catch (e: Exception) {
            null
        }
    }

    fun validarTipo(tipo: String): String? =
        if (tipo.isBlank()) "Selecciona el tipo de gestión" else null

    fun validarObservacion(observacion: String): String? = when {
        observacion.isBlank() -> "La observación es obligatoria"
        observacion.trim().length < 5 -> "Debe tener al menos 5 caracteres"
        observacion.length > 200 -> "Máximo 200 caracteres"
        else -> null
    }

    // Campo opcional: si se completa, debe ser una fecha real y no anterior a hoy
    fun validarProximaFecha(fecha: String): String? {
        if (fecha.isBlank()) return null

        val parseada = parsearFecha(fecha.trim())
            ?: return "Usa el formato dd/mm/aaaa con una fecha real"

        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val hoy = formato.parse(formato.format(Date()))

        return if (hoy != null && parseada.before(hoy)) "No puede ser anterior a hoy" else null
    }
}