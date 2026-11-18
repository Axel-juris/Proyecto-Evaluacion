package com.example.proyecto_evaluacion.data

data class Seguimiento(
    val id: Int = 0,
    val oportunidadId: Int,
    val ejecutivoId: Int,
    val fecha: String,
    val tipoGestion: String, // "LLAMADA", "REUNION", "CORREO", "VISITA"
    val observacion: String,
    val proximaGestionFecha: String? = null
)