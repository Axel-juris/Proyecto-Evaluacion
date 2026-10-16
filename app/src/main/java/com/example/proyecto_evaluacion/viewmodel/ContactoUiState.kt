package com.example.proyecto_evaluacion.viewmodel

data class ContactoUiState(
    val organizacionId: Int? = null,
    val organizacionNombre: String = "",
    val nombre: String = "",
    val cargo: String = "",
    val area: String = "",
    val correo: String = "",
    val telefono: String = "",
    val medio: String = "",
    val observaciones: String = "",

    val errorOrganizacion: String? = null,
    val errorNombre: String? = null,
    val errorCargo: String? = null,
    val errorArea: String? = null,
    val errorCorreo: String? = null,
    val errorTelefono: String? = null,
    val errorMedio: String? = null,
    val errorObservaciones: String? = null,

    val errorOperacion: String? = null,
    val guardadoExitoso: Boolean = false
)