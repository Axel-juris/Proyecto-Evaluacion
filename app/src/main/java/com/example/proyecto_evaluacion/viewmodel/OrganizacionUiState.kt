package com.example.proyecto_evaluacion.viewmodel

data class OrganizacionUiState(
    val nombre: String = "",
    val tipo: String = "",
    val rubro: String = "",
    val comuna: String = "",
    val region: String = "",
    val sitioWeb: String = "",
    val observaciones: String = "",

    val errorNombre: String? = null,
    val errorTipo: String? = null,
    val errorRubro: String? = null,
    val errorComuna: String? = null,
    val errorRegion: String? = null,
    val errorSitioWeb: String? = null,
    val errorObservaciones: String? = null,

    val guardadoExitoso: Boolean = false
)