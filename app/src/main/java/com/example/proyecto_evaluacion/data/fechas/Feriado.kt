package com.example.proyecto_evaluacion.data.fechas

/** Un feriado entregado por la API externa Nager.Date (el resto de sus campos se ignora). */
data class Feriado(
    val date: String,      // formato ISO: "2026-12-08"
    val localName: String, // nombre en español
    val name: String       // nombre en inglés
)