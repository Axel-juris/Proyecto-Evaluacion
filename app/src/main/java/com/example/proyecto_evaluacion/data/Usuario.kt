package com.example.proyecto_evaluacion.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val correo: String,
    val password: String,
    val rol: String // "SUPERVISOR" o "EJECUTIVO"
)