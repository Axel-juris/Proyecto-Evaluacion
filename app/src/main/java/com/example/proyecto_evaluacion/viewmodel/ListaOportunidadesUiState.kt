package com.example.proyecto_evaluacion.viewmodel

import com.example.proyecto_evaluacion.data.Oportunidad

const val TODOS = "TODOS"

// Filtros aparte del resto del estado, para que el campo de búsqueda responda al instante
data class FiltrosLista(
    val busqueda: String = "",
    val estado: String = TODOS,
    val prioridad: String = TODOS
)

data class OportunidadItem(
    val oportunidad: Oportunidad,
    val organizacionNombre: String,
    val responsable: String
)

data class ListaOportunidadesUiState(
    val items: List<OportunidadItem> = emptyList(),
    val totalSinFiltrar: Int = 0,
    val cargando: Boolean = true
)
