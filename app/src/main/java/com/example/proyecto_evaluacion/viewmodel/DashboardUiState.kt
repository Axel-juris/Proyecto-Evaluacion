package com.example.proyecto_evaluacion.viewmodel

data class ItemDashboard(
    val oportunidadId: Int,
    val titulo: String,
    val organizacion: String,
    val detalle: String
)

data class DashboardUiState(
    val cargando: Boolean = true,
    val totalActivas: Int = 0,
    val totalSinAsignar: Int = 0,
    val proximos: List<ItemDashboard> = emptyList(),
    val pendientes: List<ItemDashboard> = emptyList(),
    val atrasadas: List<ItemDashboard> = emptyList(),
    val sinGestion: List<ItemDashboard> = emptyList()
)