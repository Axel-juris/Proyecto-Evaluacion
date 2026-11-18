package com.example.proyecto_evaluacion.data.remote

/** Cuerpos de petición que no son una entidad completa. */
data class LoginRequest(
    val correo: String,
    val password: String
)

data class EstadoRequest(
    val estado: String
)

data class AsignacionRequest(
    val ejecutivoId: Int
)