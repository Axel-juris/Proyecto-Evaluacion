package com.example.proyecto_evaluacion.validation

// Reglas de negocio: qué puede hacer cada rol con una oportunidad
object ReglasEstado {

    fun transicionesPermitidas(estadoActual: String, rol: String): List<String> {
        val esSupervisor = rol == "SUPERVISOR"

        return when (estadoActual) {
            // El ejecutivo puede iniciar la gestión; solo el supervisor puede descartar
            "NUEVA" ->
                if (esSupervisor) listOf("EN_GESTION", "DESCARTADA") else listOf("EN_GESTION")

            // Confirmar o descartar es decisión exclusiva del supervisor
            "EN_GESTION" ->
                if (esSupervisor) listOf("CONFIRMADA", "DESCARTADA") else emptyList()

            // Los estados finales solo los puede reabrir el supervisor
            "CONFIRMADA", "DESCARTADA" ->
                if (esSupervisor) listOf("EN_GESTION") else emptyList()

            else -> emptyList()
        }
    }

    // Edición de datos: el supervisor siempre; el ejecutivo solo si es suya y sigue abierta
    fun puedeEditar(
        rol: String?,
        usuarioId: Int?,
        ejecutivoId: Int?,
        estado: String
    ): Boolean = when (rol) {
        "SUPERVISOR" -> true
        "EJECUTIVO" ->
            usuarioId != null &&
                    ejecutivoId == usuarioId &&
                    (estado == "NUEVA" || estado == "EN_GESTION")
        else -> false
    }
}