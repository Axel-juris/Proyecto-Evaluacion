package com.example.proyecto_evaluacion.validation

// Reglas de negocio: qué cambios de estado puede hacer cada rol
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
}