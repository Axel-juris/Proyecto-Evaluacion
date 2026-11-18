package com.example.proyecto_evaluacion.data

data class Organizacion(
    val id: Int = 0,
    val nombre: String,
    val tipo: String,
    val rubro: String,
    val comuna: String,
    val region: String,
    val sitioWeb: String = "",
    val observaciones: String = ""
)