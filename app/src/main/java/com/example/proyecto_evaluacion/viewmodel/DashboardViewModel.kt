package com.example.proyecto_evaluacion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_evaluacion.data.CrmRepository
import com.example.proyecto_evaluacion.data.Oportunidad
import com.example.proyecto_evaluacion.data.SessionManager
import com.example.proyecto_evaluacion.fechas.Fechas
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CrmRepository.getInstance(application)
    private val usuario = SessionManager.usuarioActual.value

    // Restricción por rol: el supervisor ve todas; el ejecutivo solo las asignadas a él
    private val fuente: Flow<List<Oportunidad>> =
        if (usuario?.rol == "SUPERVISOR") {
            repository.getTodasOportunidades()
        } else if (usuario != null) {
            repository.getOportunidadesPorEjecutivo(usuario.id)
        } else {
            flowOf(emptyList())
        }

    val uiState: StateFlow<DashboardUiState> = combine(
        fuente,
        repository.getTodosSeguimientos(),
        repository.getOrganizaciones()
    ) { oportunidades, seguimientos, organizaciones ->

        val nombres = organizaciones.associate { it.id to it.nombre }

        fun item(oportunidad: Oportunidad, detalle: String) = ItemDashboard(
            oportunidadId = oportunidad.id,
            titulo = oportunidad.titulo,
            organizacion = nombres[oportunidad.organizacionId] ?: "Sin organización",
            detalle = detalle
        )

        // Activas: las que todavía se están gestionando
        val activas = oportunidades.filter { it.estado == "NUEVA" || it.estado == "EN_GESTION" }

        // Eventos próximos: desde hoy hasta 14 días, sin contar las descartadas
        val proximos = oportunidades
            .filter { it.estado != "DESCARTADA" }
            .mapNotNull { oportunidad ->
                val dias = Fechas.diasDesdeHoy(oportunidad.fechaEvento)
                if (dias != null && dias in 0..14) oportunidad to dias else null
            }
            .sortedBy { it.second }
            .map { (oportunidad, dias) ->
                item(oportunidad, "${oportunidad.fechaEvento} · ${textoFuturo(dias)}")
            }

        // Pendiente o atrasada según la próxima gestión de la última gestión registrada
        val pendientes = mutableListOf<Triple<Oportunidad, Int, String>>()
        val atrasadas = mutableListOf<Triple<Oportunidad, Int, String>>()
        val sinGestion = mutableListOf<Oportunidad>()

        activas.forEach { oportunidad ->
            val ultimo = seguimientos
                .filter { it.oportunidadId == oportunidad.id }
                .maxByOrNull { it.id }

            if (ultimo == null) {
                sinGestion.add(oportunidad)
            } else {
                val fecha = ultimo.proximaGestionFecha
                val dias = Fechas.diasDesdeHoy(fecha)
                if (dias != null && fecha != null) {
                    if (dias < 0) {
                        atrasadas.add(Triple(oportunidad, dias, fecha))
                    } else {
                        pendientes.add(Triple(oportunidad, dias, fecha))
                    }
                }
            }
        }

        DashboardUiState(
            cargando = false,
            totalActivas = activas.size,
            totalSinAsignar = oportunidades.count {
                it.ejecutivoId == null && it.estado != "DESCARTADA"
            },
            proximos = proximos,
            pendientes = pendientes
                .sortedBy { it.second }
                .map { (oportunidad, dias, fecha) ->
                    item(oportunidad, "Próxima gestión: $fecha (${textoFuturo(dias)})")
                },
            atrasadas = atrasadas
                .sortedBy { it.second }
                .map { (oportunidad, dias, fecha) ->
                    item(oportunidad, "Gestión atrasada: $fecha (${textoPasado(-dias)})")
                },
            sinGestion = sinGestion.map { oportunidad ->
                item(oportunidad, "Detectada el ${oportunidad.fechaCreacion}, sin gestiones registradas")
            }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardUiState()
    )

    private fun textoFuturo(dias: Int): String = when (dias) {
        0 -> "hoy"
        1 -> "mañana"
        else -> "en $dias días"
    }

    private fun textoPasado(dias: Int): String =
        if (dias == 1) "hace 1 día" else "hace $dias días"
}