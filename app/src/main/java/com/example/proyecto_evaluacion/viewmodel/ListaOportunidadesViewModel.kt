package com.example.proyecto_evaluacion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_evaluacion.data.CrmRepository
import com.example.proyecto_evaluacion.data.Oportunidad
import com.example.proyecto_evaluacion.data.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class ListaOportunidadesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CrmRepository.getInstance(application)

    private val usuario = SessionManager.usuarioActual.value

    private val _filtros = MutableStateFlow(FiltrosLista())
    val filtros: StateFlow<FiltrosLista> = _filtros.asStateFlow()

    // Restricción por rol: el supervisor ve todas; el ejecutivo solo las asignadas a él
    private val fuente: Flow<List<Oportunidad>> =
        if (usuario?.rol == "SUPERVISOR") {
            repository.getTodasOportunidades()
        } else if (usuario != null) {
            repository.getOportunidadesPorEjecutivo(usuario.id)
        } else {
            flowOf(emptyList())
        }

    val uiState: StateFlow<ListaOportunidadesUiState> = combine(
        fuente,
        repository.getOrganizaciones(),
        repository.getEjecutivos(),
        _filtros
    ) { oportunidades, organizaciones, ejecutivos, filtros ->

        val nombresOrganizacion = organizaciones.associate { it.id to it.nombre }
        val nombresEjecutivo = ejecutivos.associate { it.id to it.nombre }
        val texto = filtros.busqueda.trim()

        val filtradas = oportunidades.filter { oportunidad ->
            val nombreOrg = nombresOrganizacion[oportunidad.organizacionId] ?: ""

            val coincideTexto = texto.isEmpty() ||
                    oportunidad.titulo.contains(texto, ignoreCase = true) ||
                    nombreOrg.contains(texto, ignoreCase = true)
            val coincideEstado = filtros.estado == TODOS || oportunidad.estado == filtros.estado
            val coincidePrioridad =
                filtros.prioridad == TODOS || oportunidad.prioridad == filtros.prioridad

            coincideTexto && coincideEstado && coincidePrioridad
        }

        ListaOportunidadesUiState(
            items = filtradas.map { oportunidad ->
                OportunidadItem(
                    oportunidad = oportunidad,
                    organizacionNombre = nombresOrganizacion[oportunidad.organizacionId]
                        ?: "Sin organización",
                    responsable = oportunidad.ejecutivoId
                        ?.let { nombresEjecutivo[it] ?: "Desconocido" }
                        ?: "Sin asignar"
                )
            },
            totalSinFiltrar = oportunidades.size,
            cargando = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ListaOportunidadesUiState()
    )

    fun cambiarBusqueda(valor: String) {
        _filtros.update { it.copy(busqueda = valor) }
    }

    fun cambiarEstado(valor: String) {
        _filtros.update { it.copy(estado = valor) }
    }

    fun cambiarPrioridad(valor: String) {
        _filtros.update { it.copy(prioridad = valor) }
    }

    fun limpiarFiltros() {
        _filtros.value = FiltrosLista()
    }
}