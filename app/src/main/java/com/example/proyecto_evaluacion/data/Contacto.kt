package com.example.proyecto_evaluacion.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contactos",
    foreignKeys = [
        ForeignKey(
            entity = Organizacion::class,
            parentColumns = ["id"],
            childColumns = ["organizacionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("organizacionId")]
)
data class Contacto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val organizacionId: Int,
    val nombre: String,
    val cargo: String,
    val area: String,
    val correo: String,
    val telefono: String,
    val medioPreferente: String,
    val observaciones: String = ""
)