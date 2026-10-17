package com.example.proyecto_evaluacion.fechas

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object Fechas {

    private const val FORMATO = "dd/MM/yyyy"

    fun parsear(texto: String?): Date? {
        if (texto.isNullOrBlank()) return null
        val formato = SimpleDateFormat(FORMATO, Locale.getDefault())
        formato.isLenient = false
        return try {
            formato.parse(texto.trim())
        } catch (e: Exception) {
            null
        }
    }

    // Hoy a las 00:00, para comparar solo por día
    fun hoy(): Date {
        val calendario = Calendar.getInstance()
        calendario.set(Calendar.HOUR_OF_DAY, 0)
        calendario.set(Calendar.MINUTE, 0)
        calendario.set(Calendar.SECOND, 0)
        calendario.set(Calendar.MILLISECOND, 0)
        return calendario.time
    }

    // Días desde hoy hasta la fecha: negativo si ya pasó, null si el texto no es una fecha válida
    fun diasDesdeHoy(texto: String?): Int? {
        val fecha = parsear(texto) ?: return null
        val diferencia = fecha.time - hoy().time
        return Math.round(diferencia / 86_400_000.0).toInt()
    }

    // Fecha y hora locales ("dd/MM/yyyy" + "HH:mm") como milisegundos, para eventos con hora
    fun milisLocal(fecha: String?, hora: String): Long? {
        if (fecha.isNullOrBlank()) return null
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        formato.isLenient = false
        return try {
            formato.parse("${fecha.trim()} ${hora.trim()}")?.time
        } catch (e: Exception) {
            null
        }
    }

    // Inicio del día en UTC: así lo exige el calendario para los eventos de todo el día
    fun milisInicioDiaUtc(fecha: String?): Long? {
        if (fecha.isNullOrBlank()) return null
        val formato = SimpleDateFormat(FORMATO, Locale.getDefault())
        formato.isLenient = false
        formato.timeZone = TimeZone.getTimeZone("UTC")
        return try {
            formato.parse(fecha.trim())?.time
        } catch (e: Exception) {
            null
        }
    }
}