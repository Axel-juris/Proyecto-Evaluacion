package com.example.proyecto_evaluacion.viewmodel

import com.example.proyecto_evaluacion.data.fechas.Feriado

data class FeriadosUiState(
    val cargando: Boolean = true,
    val feriados: List<Feriado> = emptyList(),
    val error: String? = null
)