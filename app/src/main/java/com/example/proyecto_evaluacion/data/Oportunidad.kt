package com.example.proyecto_evaluacion.data

data class Oportunidad(
    val id: Int = 0,
    val titulo: String,
    val descripcion: String,
    val monto: Double,
    val estado: String, // "NUEVA", "EN_GESTION", "CONFIRMADA", "DESCARTADA"
    val prioridad: String, // "ALTA", "MEDIA", "BAJA"
    val fechaCreacion: String,
    val fechaEvento: String,
    val fechaTermino: String,
    val horario: String,
    val lugarEvento: String,
    val tipoEvento: String,
    val publicoEsperado: Int,
    val organizacionId: Int,
    val contactoId: Int?,
    val ejecutivoId: Int?, // Responsable asignado
    val fotoUri: String? = null
)