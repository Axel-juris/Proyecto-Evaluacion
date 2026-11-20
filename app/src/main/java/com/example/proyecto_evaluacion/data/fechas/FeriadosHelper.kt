package com.example.proyecto_evaluacion.fechas

import com.example.proyecto_evaluacion.data.fechas.Feriado

/** Lógica pura para trabajar con la lista de feriados (fácil de probar sin internet). */
object FeriadosHelper {

    /**
     * Devuelve los próximos feriados a partir de [hoyIso] (inclusive), ordenados por fecha.
     * Las fechas ISO (aaaa-mm-dd) se pueden comparar directamente como texto.
     */
    fun proximos(feriados: List<Feriado>, hoyIso: String, cantidad: Int = 3): List<Feriado> =
        feriados
            .filter { it.date >= hoyIso }
            .sortedBy { it.date }
            .take(cantidad)

    /** Convierte "2026-12-08" en "08/12/2026", el formato que usa el resto de la app. */
    fun formatear(fechaIso: String): String {
        val partes = fechaIso.split("-")
        return if (partes.size == 3) "${partes[2]}/${partes[1]}/${partes[0]}" else fechaIso
    }
}