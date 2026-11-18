package com.example.proyecto_evaluacion.data

data class Usuario(
    val id: Int = 0,
    val nombre: String,
    val correo: String,
    // El microservicio nunca devuelve la clave, por eso es opcional
    val password: String? = null,
    val rol: String // "SUPERVISOR" o "EJECUTIVO"
)