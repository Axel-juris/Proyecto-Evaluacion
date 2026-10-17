package com.example.proyecto_evaluacion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_evaluacion.data.CrmRepository
import com.example.proyecto_evaluacion.data.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CargaEjecutivo(
    val nombre: String,
    val activas: Int,
    val total: Int
)

data class PanelSupervisorUiState(
    val cargando: Boolean = true,
    val autorizado: Boolean = true,
    val totalOportunidades: Int = 0,
    val porEstado: Map<String, Int> = emptyMap(),
    val cargaEjecutivos: List<CargaEjecutivo> = emptyList(),
    val sinAsignar: List<ItemDashboard> = emptyList()
)

class PanelSupervisorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CrmRepository.getInstance(application)

    // Si no es supervisor, ni siquiera se consultan los datos globales
    val uiState: StateFlow<PanelSupervisorUiState> = if (!SessionManager.esSupervisor()) {
        MutableStateFlow(PanelSupervisorUiState(cargando = false, autorizado = false))
    } else {
        combine(
            repository.getTodasOportunidades(),
            repository.getEjecutivos(),
            repository.getOrganizaciones()
        ) { oportunidades, ejecutivos, organizaciones ->

            val nombres = organizaciones.associate { it.id to it.nombre }

            PanelSupervisorUiState(
                cargando = false,
                autorizado = true,
                totalOportunidades = oportunidades.size,
                porEstado = oportunidades.groupingBy { it.estado }.eachCount(),
                cargaEjecutivos = ejecutivos.map { ejecutivo ->
                    val suyas = oportunidades.filter { it.ejecutivoId == ejecutivo.id }
                    CargaEjecutivo(
                        nombre = ejecutivo.nombre,
                        activas = suyas.count { it.estado == "NUEVA" || it.estado == "EN_GESTION" },
                        total = suyas.size
                    )
                },
                sinAsignar = oportunidades
                    .filter { it.ejecutivoId == null && it.estado != "DESCARTADA" }
                    .map { oportunidad ->
                        ItemDashboard(
                            oportunidadId = oportunidad.id,
                            titulo = oportunidad.titulo,
                            organizacion = nombres[oportunidad.organizacionId] ?: "Sin organización",
                            detalle = "${oportunidad.tipoEvento} · ${oportunidad.fechaEvento}"
                        )
                    }
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            PanelSupervisorUiState()
        )
    }
}