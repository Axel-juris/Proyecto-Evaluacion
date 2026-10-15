package com.example.proyecto_evaluacion.viewmodel

data class ContactoUiState(
    val nombre: String = "",
    val organizacion: String = "",
    val cargo: String = "",
    val area: String = "",
    val correo: String = "",
    val telefono: String = "",
    val medio: String = "",
    val observaciones: String = "",

    val errorNombre: String? = null,
    val errorOrganizacion: String? = null,
    val errorCargo: String? = null,
    val errorArea: String? = null,
    val errorCorreo: String? = null,
    val errorTelefono: String? = null,
    val errorMedio: String? = null,
    val errorObservaciones: String? = null,

    val guardadoExitoso: Boolean = false
)