package com.example.proyecto_evaluacion.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "organizaciones")
data class Organizacion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val tipo: String,
    val rubro: String,
    val comuna: String,
    val region: String,
    val sitioWeb: String = "",
    val observaciones: String = ""
)