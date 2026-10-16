package com.example.proyecto_evaluacion.viewmodel

data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val errorCorreo: String? = null,
    val errorClave: String? = null,
    val errorLogin: String? = null,
    val cargando: Boolean = false
)