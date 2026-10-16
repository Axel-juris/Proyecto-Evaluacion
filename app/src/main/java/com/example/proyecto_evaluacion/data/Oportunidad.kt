package com.example.proyecto_evaluacion.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "oportunidades",
    foreignKeys = [
        ForeignKey(
            entity = Organizacion::class,
            parentColumns = ["id"],
            childColumns = ["organizacionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Contacto::class,
            parentColumns = ["id"],
            childColumns = ["contactoId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["ejecutivoId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("organizacionId"), Index("contactoId"), Index("ejecutivoId")]
)
data class Oportunidad(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val descripcion: String,
    val monto: Double,
    val estado: String, // "NUEVA", "EN_GESTION", "CONFIRMADA", "DESCARTADA"
    val prioridad: String, // "ALTA", "MEDIA", "BAJA"
    val fechaCreacion: String,
    val fechaEvento: String,
    val lugarEvento: String,
    val tipoEvento: String,
    val publicoEsperado: Int,
    val organizacionId: Int,
    val contactoId: Int?,
    val ejecutivoId: Int?, // Responsable asignado
    val fotoUri: String? = null
)