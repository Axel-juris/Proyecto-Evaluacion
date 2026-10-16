package com.example.proyecto_evaluacion.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "organizaciones")
data class Organizacion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rut: String,
    val nombre: String,
    val rubro: String,
    val direccion: String,
    val telefono: String
)