package com.example.proyecto_evaluacion.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "seguimientos",
    foreignKeys = [
        ForeignKey(
            entity = Oportunidad::class,
            parentColumns = ["id"],
            childColumns = ["oportunidadId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["ejecutivoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("oportunidadId"), Index("ejecutivoId")]
)
data class Seguimiento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val oportunidadId: Int,
    val ejecutivoId: Int,
    val fecha: String,
    val tipoGestion: String, // "LLAMADA", "REUNION", "CORREO", "VISITA"
    val observacion: String,
    val proximaGestionFecha: String? = null
)