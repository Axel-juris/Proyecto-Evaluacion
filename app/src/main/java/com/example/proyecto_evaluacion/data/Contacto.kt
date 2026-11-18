package com.example.proyecto_evaluacion.data

data class Contacto(
    val id: Int = 0,
    val organizacionId: Int,
    val nombre: String,
    val cargo: String,
    val area: String,
    val correo: String,
    val telefono: String,
    val medioPreferente: String,
    val observaciones: String = ""
)